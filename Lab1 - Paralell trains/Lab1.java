import TSim.*;

import java.math.BigInteger;
import java.util.concurrent.Semaphore;

public class Lab1 {

    Semaphore[] stations = {
            new Semaphore(1), // 0 - South station bottom
            new Semaphore(0), // 1 - South station top
            new Semaphore(1), // 2 - North station bottom
            new Semaphore(1), // 3 - North station top
    };

    Semaphore[] criticalRegion = {
            new Semaphore(1), // 0 - Far left
            new Semaphore(1), // 1 - Far right
            new Semaphore(1), // 2 - Cross section
    };

    Semaphore[] splitRegion = {
            new Semaphore(1), // 0 - Mid section
    };

    // CLASSES
    class Train implements Runnable {

        // variables for the train
        final int id; // TrainID -- what train is calling. 1 || 2
        int speed; // Speed -- how fast should the train run. maxspeed 22(tested)
        boolean atStation; // atStation -- true/false -- is the train currently in a station
        String currentRegion; // currentRegion -- far right || far left || cross section || or no station --
        // telling the train where it is
        int lastStation; // lastStaion -- 1 || 2 || 3 || 4 -- telling the train which station it was on
        // last
        boolean mainPath; // mainPath --True/false -- telling the train if its on the mainPath i.e. the
        // shortest Path

        // Constructor for the train -- initialize the values needed at the start
        public Train(int id, int speed) {
            this.id = id;
            this.speed = speed;
            lastStation = id == 1 ? 3 : 0;
            atStation = true;
            currentRegion = "";
            mainPath = false;
        }

        /**
         *
         * @param e   -- what event called the method
         * @param tsi -- the interface
         * @throws CommandException
         * @throws InterruptedException
         *
         *                              This method checks what sensors got triggered
         *                              and based on the sensors it passes the values to
         *                              the correct method.
         */
        private void SensorHandler(SensorEvent e, TSimInterface tsi) throws CommandException, InterruptedException {
            // Train stations
            // South station top track

            if (e.getXpos() == 14 && e.getYpos() == 11) {
                if (!this.atStation)
                    turnAround(tsi);
                this.lastStation = 1;
                // North station bottom track
            } else if (e.getXpos() == 14 && e.getYpos() == 5) {
                if (!this.atStation)
                    turnAround(tsi);
                this.lastStation = 2;
                // North station top track
            } else if (e.getXpos() == 14 && e.getYpos() == 3) {
                if (!this.atStation)
                    turnAround(tsi);
                this.lastStation = 3;
                // South station top track
            } else if (e.getXpos() == 14 && e.getYpos() == 13) {
                if (!this.atStation)
                    turnAround(tsi);
                this.lastStation = 0;
            } else {
                enterCritical(e, tsi);
                twoWaySections(e, tsi);
                this.atStation = false;
            }

        }

        /**
         *
         * @param tsi -- Interface
         * @throws InterruptedException
         * @throws CommandException
         *
         *                              If the train is at the station wait 1 - 2
         *                              seconds and turn it around(this case backing)
         */
        private void turnAround(TSimInterface tsi) throws InterruptedException, CommandException {
            tsi.setSpeed(this.id, 0);
            Thread.sleep(1000 + (20 * Math.abs(this.speed)));
            this.speed = this.speed * -1;
            tsi.setSpeed(this.id, this.speed);
            this.atStation = true;
        }

        /**
         *
         * @param e   -- SensorEvent
         * @param tsi -- Interface
         * @throws CommandException
         * @throws InterruptedException This method checks if the train can continue to
         *                              the critical areas on the track by checking if
         *                              the semaphores are taken or not. based on what
         *                              sensors gets triggered and if the area is not
         *                              taken it changes the currentRegion on the train
         *                              to what semaphore it has taken. if the train
         *                              can't take the semaphore it waits until it can
         *                              acquire it before it moves. If the train still
         *                              has the critical area when passing one of the
         *                              outgoing sensors it releases the semaphore.
         */
        private void enterCritical(SensorEvent e, TSimInterface tsi) throws CommandException, InterruptedException {
            // Far left region
            if ((e.getXpos() >= 4 && e.getXpos() <= 7) && (e.getYpos() >= 9 && e.getYpos() <= 13) && e.getStatus() == 1){
                // Release the far left semaphore
                if (this.currentRegion.equals("Far left")) {
                    criticalRegion[0].release();
                    this.currentRegion = "";
                } else {
                    // Acquired Far left semaphore
                    if (criticalRegion[0].tryAcquire()) {
                        this.currentRegion = "Far left";

                    } else {
                        // Can't acquire the semaphore so it waits until it gets it before it moves
                        tsi.setSpeed(this.id, 0);
                        criticalRegion[0].acquire();
                        this.currentRegion = "Far left";
                        tsi.setSpeed(this.id, this.speed);
                    }

                }

                // Far right region
            } else if ((e.getXpos() >= 12 && e.getXpos() <= 15) && (e.getYpos() >= 7 && e.getYpos() <= 10) && e.getStatus() == 1) {
                // release the semaphore far right
                if (this.currentRegion.equals("Far right")) {
                    criticalRegion[1].release();
                    this.currentRegion = "";
                } else {
                    // Acquired Far right
                    if (criticalRegion[1].tryAcquire()) {
                        this.currentRegion = "Far right";

                    } else {
                        // Can't acquire the semaphore so it waits until it gets it before it moves
                        tsi.setSpeed(this.id, 0);
                        criticalRegion[1].acquire();
                        this.currentRegion = "Far right";
                        tsi.setSpeed(this.id, this.speed);
                    }

                }

            } else if ((e.getXpos() >= 6 && e.getXpos() <= 11) && (e.getYpos() >= 5 && e.getYpos() <= 8) && e.getStatus() == 1) {
                // Release cross section
                if (this.currentRegion.equals("Cross section")) {
                    criticalRegion[2].release();
                    this.currentRegion = "";
                } else {
                    // Acquired cross section
                    if (criticalRegion[2].tryAcquire()) {
                        this.currentRegion = "Cross section";

                    } else {
                        // Can't acquire the semaphore so it waits until it gets it before it moves
                        tsi.setSpeed(this.id, 0);
                        criticalRegion[2].acquire();
                        this.currentRegion = "Cross section";
                        tsi.setSpeed(this.id, this.speed);
                    }

                }

            }
        }

        /**
         *
         * @param e   -- SensorEvent
         * @param tsi -- Interface
         * @throws CommandException
         *
         *                          This Method checks if the train can take the
         *                          shortest path or not. based on if it can or not it
         *                          will take the short path or the longer path and
         *                          telling the train if its on the shortest
         *                          path(mainpath) or not
         */
        private void twoWaySections(SensorEvent e, TSimInterface tsi) throws CommandException {
            if (e.getXpos() == 6 && e.getYpos() == 11 && e.getStatus() == 1
                    || e.getXpos() == 4 && e.getYpos() == 13 && e.getStatus() == 1) {// SOUTH STATION
                if (this.lastStation < 2) {
                    tsi.setSwitch(3, 11, (this.lastStation == 1) ? 1 : 0);
                    // trying to take the middle semaphore(short path) if not take the long way

                    stations[this.lastStation].release(); //Increasing unused semaphores infinitely (If it's a problem just add another if to make sure it's either 1 or 2)
                } else if (this.mainPath) {
                    this.mainPath = false;
                    splitRegion[0].release();
                }
            } else if (e.getXpos() == 13 && e.getYpos() == 10 && e.getStatus() == 1 // Midsection right
                    || e.getXpos() == 12 && e.getYpos() == 9 && e.getStatus() == 1) {
                if (this.lastStation < 2) {
                    tsi.setSwitch(15, 9, (e.getYpos() == 10) ? 1 : 0);

                }
            } else if (e.getXpos() == 15 && e.getYpos() == 8 && e.getStatus() == 1 // North Station
                    || e.getXpos() == 14 && e.getYpos() == 7 && e.getStatus() == 1) {
                if (this.lastStation > 1) { // >1 == visited the north stations and is now heading out
                    tsi.setSwitch(17, 7, (this.lastStation == 2) ? 1 : 0);
                    // trying to take the middle semaphore(short path) if not take the long way

                    stations[this.lastStation].release(); //Increasing unused semaphores infinitely (If it's a problem just add another if to make sure it's either 1 or 2)
                }
            } else if (e.getXpos() == 6 && e.getYpos() == 10 && e.getStatus() == 1 // Midsection left
                    || e.getXpos() == 7 && e.getYpos() == 9 && e.getStatus() == 1) {
                // try to take the south station top track
                if (this.lastStation > 1) {
                    tsi.setSwitch(4, 9, (e.getYpos() == 10) ? 0 : 1);
                }
            } else if (e.getXpos() == 3 && e.getYpos() == 9 && e.getStatus() == 1) {
                if (this.lastStation < 2) {
                     if (splitRegion[0].tryAcquire()) {
                        this.mainPath = true;
                        tsi.setSwitch(4, 9, 1);
                    } else {
                        this.mainPath = false;
                        tsi.setSwitch(4, 9, 0);
                    }

                } else if (this.mainPath) {
                    this.mainPath = false;
                    splitRegion[0].release();
                }
            } else if (e.getXpos() == 16 && e.getYpos() == 9 && e.getStatus() == 1) {
                if (this.lastStation > 1) {

                    if (splitRegion[0].tryAcquire()) {
                        this.mainPath = true;
                        tsi.setSwitch(15, 9, 0);
                    } else {
                        this.mainPath = false;
                        tsi.setSwitch(15, 9, 1);
                    }
                } else
                if (this.mainPath) {
                    this.mainPath = false;
                    splitRegion[0].release();
                }
            } else if (e.getXpos() == 2 && e.getYpos() == 11 && e.getStatus() == 1) {
                if (this.lastStation > 1) {
                    if (stations[1].tryAcquire()) {
                        tsi.setSwitch(3, 11, 1);
                    } else
                        tsi.setSwitch(3, 11, 0);
                 }

            } else if (e.getXpos() == 18 && e.getYpos() == 7 && e.getStatus() == 1) {
                if (this.lastStation < 2) {
                    if (stations[2].tryAcquire()) {
                        tsi.setSwitch(17, 7, 1);
                    } else
                        tsi.setSwitch(17, 7, 0);

                }
            }
        }

        @Override
        public void run() {
            TSimInterface tsi = TSimInterface.getInstance();

            // Start train
            try {
                tsi.setSpeed(id, speed);
            } catch (CommandException e) {
                e.printStackTrace();
                System.out.println("FEL " + e);
            }

            // Search for area change
            while (true) {
                SensorEvent se = null;

                try {
                    se = tsi.getSensor(this.id);
                } catch (CommandException | InterruptedException e) {
                    e.printStackTrace();
                }

                if (se != null) {
                    try {
                        SensorHandler(se, tsi);
                    } catch (CommandException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    // END OF CLASSES

    public Lab1(int speed1, int speed2) {

        // Create one thread for each train
        Thread train1 = new Thread(new Train(1, speed1));
        Thread train2 = new Thread(new Train(2, speed2));
        train1.start();
        train2.start();

    }
}
