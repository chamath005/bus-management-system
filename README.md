<img width="4646" height="7080" alt="mermaid-diagram" src="https://github.com/user-attachments/assets/dc142c3c-1691-4ee9-9997-6674fe67f29a" />
# Bus Fleet Management & Passenger System (IIC 1153 Group Assignment)

## Problem
New private bus owners need one system that serves **passengers, conductors, drivers and owners**:
owners must see passengers on each bus, whether every passenger has a ticket, routes and busiest route,
speed, breakdowns and replacement buses; passengers want seat booking, cashless payment and bus tracking.

## How to run in IntelliJ IDEA
1. Unzip, then **File > Open** and choose the `BusTrackingSystem` folder.
2. If IntelliJ says "Project SDK is not defined": **File > Project Structure > Project > SDK**, pick JDK 17 or newer.
3. Open `src/com/busapp/App.java` and click the green arrow next to `main` > **Run 'App.main()'**.
4. Use the **Run** window at the bottom to type (click in it first).
Data is saved in the `data/` folder. To reset everything, stop the program and delete that folder.

## Demo logins (created on first run)
| Role | Phone | Password |
|---|---|---|
| Owner | 0770000001 | owner123 |
| Driver (bus NB-1001) | 0771111111 | driver123 |
| Driver (bus NB-1002) | 0771111112 | driver123 |
| Conductor (bus NB-1001) | 0772222221 | conduct123 |
| Conductor (bus NB-1002) | 0772222222 | conduct123 |
| Passenger (Rs.2000 wallet) | 0773333333 | pass123 |

New users can register as Passenger, Driver, Conductor or Owner. Owner registration needs the key `OWNER2026`.
After login each role gets **only its own menu**.

## Features by role
**Passenger:** browse routes/buses, book a seat (seat map), pay with wallet or card (no cash), top-up wallet,
cancel before boarding (refund), live bus tracking with ETA, ticket receipt file, report a lost item
(conductor is notified; passenger is notified when found), notifications.

**Driver:** bus/trip status, passengers on the bus, update speed (owner alerted if over 60 km/h),
arrive at next stop (passengers get off automatically), report breakdown / request replacement bus,
view own commission (2% of fares on the bus).

**Conductor:** passengers on the bus and seat map, issue cash ticket to walk-in passengers,
verify tickets, record physical headcount (owner sees if anyone has no ticket), handle lost-item reports,
view own commission (3% of fares).

**Owner:** fleet overview (passengers inside, speed, status, crew, ticket-vs-headcount warning), track a bus,
add routes and buses, assign route and crew, passengers per route and **busiest route**, revenue report saved to file,
staff commissions, approve/reject breakdown requests (crew, route position and all passenger tickets move to a spare bus),
mark bus repaired, speed alerts, lost-item reports.

## OOP requirements
| Requirement | Where |
|---|---|
| Encapsulation | private fields + validation in `model` classes; passwords stored only as salted SHA-256 hashes |
| Abstraction | `abstract User`, `abstract CrewMember`, `abstract RoleMenu` |
| Inheritance | `Passenger/CrewMember/Owner extends User`; `Driver/Conductor extends CrewMember` |
| Polymorphism | `getCommissionRate()` (Driver 2%, Conductor 3%); `PaymentMethod.pay()`; each role's `RoleMenu.items()` |
| Exception handling | `exception` package: `InsufficientFundsException`, `SeatUnavailableException`, `AuthException` ...; input re-asked until valid |
| Collections & Generics | generic `Repository<T extends Identifiable>`; `LinkedHashMap`, `TreeMap`, `List`, `Optional`, streams |
| File handling | `DataStorage` (object streams, safe temp-file write), receipts and revenue report text files |
| Interfaces & Packages | `Identifiable`, `Commissionable`, `PaymentMethod`; packages `model, service, payment, storage, exception, ui, util` |

## Git workflow (assignment task 5)
```
git init
git add .
git commit -m "Initial project structure and README"
git branch -M main
git remote add origin <your-repo-url>
git push -u origin main
git checkout -b feature/passenger-booking     # each member: own branch
git add src/com/busapp/service/BookingService.java
git commit -m "Add seat booking with wallet and card payment"
git push -u origin feature/passenger-booking  # then open a Pull Request and merge
```
Suggested split: (1) model + exception, (2) storage + auth, (3) fleet/tracking/breakdown, (4) booking/payment/commission,
(5) UI menus + slides. Each member should commit their own files so the history shows real contributions.




Bus Owner (Admin)
Management access
- Register and manage buses.
- View all registered buses.
- View the number of passengers currently inside each bus.
- View bus routes and assigned drivers/conductors.
- Identify the route with the highest passenger count.
- Monitor bus speed.
- Check whether a bus is broken or operational.
- Receive breakdown reports from drivers.
- Request a replacement bus.
- View total ticket revenue.
- View conductor commissions.
- Generate passenger and route reports.
2. Driver
Driver access
- View assigned bus details.
- View assigned route.
- View conductor information.
- View passenger count.
- Update trip status.
- Report bus breakdowns.
- Report accidents or delays.
- Request a replacement bus.
- View bus speed and trip information.
- View assigned schedules.
3. Conductor
Conductor access
- Issue tickets to passengers.
- Record passenger boarding and exiting.
- View current passenger count.
- View ticket sales.
- Calculate and view commission.
- View available seats.
- Identify passengers without tickets.
- Manage lost-and-found reports.
4. Passenger
Passenger access
- View available buses.
- Search bus routes.
- View ticket prices and schedules.
- Book seats.
- Cancel seat bookings.
- Make cashless payments.
- Receive digital tickets.
- Track buses using simulated GPS.
- View estimated arrival times.
- Report lost belongings.
- Submit complaints and ratings.
- Receive delay notifications.
5. Additional System Features
We also discussed adding:
- Role-based registration and login.
- Separate dashboards for each user type.
- Simulated GPS tracking.
- Simulated bus speed monitoring.
- Automatic passenger count updates.
- Automatic commission calculations.
- Route popularity statistics.
- Replacement bus management.
- Data storage using files.
- Exception handling.
- Java OOP concepts (inheritance, encapsulation, polymorphism, interfaces).
- Collections and packages.
6. Important system requirement
Role-based access control

Each user must only see the features belonging to their role.

For example:
- Passengers cannot access owner features.
- Drivers cannot manage owner settings.
- Conductors cannot access driver management.
- Owners can access overall system management.

I also remember the original goal: You wanted a proper working Java application that runs in IntelliJ, not just a collection of Java classes.

We can use this as the main requirements list when continuing development.
