package com.busapp.service;

import com.busapp.exception.*;
import com.busapp.model.*;
import com.busapp.storage.DataStore;
import java.util.List;

public class LostItemService {
    private final DataStore store;
    public LostItemService(DataStore store) { this.store = store; }

    public LostItem report(Passenger p, String busId, String description) throws BusAppException {
        Bus bus = store.buses.get(busId);
        LostItem item;
        try { item = new LostItem(store.lostItems.nextId(), p.getId(), busId, description); }
        catch (IllegalArgumentException e) { throw new ValidationException(e.getMessage()); }
        store.lostItems.add(item);
        store.notifyUser(bus.getConductorId(), "Lost item " + item.getId() + " reported on your bus: " + item.getDescription());
        store.notifyUser(bus.getOwnerId(), "Lost item " + item.getId() + " reported on bus " + bus.getPlateNo() + ".");
        return item;
    }

    public List<LostItem> forPassenger(Passenger p) { return store.lostItems.filter(l -> l.getPassengerId().equals(p.getId())); }
    public List<LostItem> openForBus(String busId) {
        return store.lostItems.filter(l -> l.getBusId().equals(busId) && l.getStatus() == LostItem.Status.OPEN);
    }
    public List<LostItem> forOwner(String ownerId) {
        return store.lostItems.filter(l -> store.buses.find(l.getBusId()).map(b -> b.getOwnerId().equals(ownerId)).orElse(false));
    }

    public void markFound(String itemId, Conductor c, String note) throws BusAppException {
        LostItem item = store.lostItems.get(itemId);
        if (!item.getBusId().equals(c.getAssignedBusId())) throw new ValidationException("That report is not for your bus.");
        if (item.getStatus() != LostItem.Status.OPEN) throw new ValidationException("Report is already " + item.getStatus() + ".");
        item.setStatus(LostItem.Status.FOUND);
        item.setNote(note);
        store.notifyUser(item.getPassengerId(), "Good news! Your lost item (" + item.getDescription() + ") was FOUND by conductor "
                + c.getName() + " (" + c.getPhone() + "). " + note);
    }

    public void close(String itemId) throws BusAppException {
        LostItem item = store.lostItems.get(itemId);
        item.setStatus(LostItem.Status.CLOSED);
    }
}
