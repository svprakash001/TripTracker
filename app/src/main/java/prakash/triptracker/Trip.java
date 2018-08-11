package prakash.triptracker;

class Trip {

    private String tripcode;
    private String tripname;

    public Trip(){
    }

    public Trip(String tripname) {
        this.tripname = tripname;
    }

    public Trip(String tripcode, String tripname) {
        this.tripcode = tripcode;
        this.tripname = tripname;
    }

    public String getTripcode() {
        return tripcode;
    }

    public void setTripcode(String tripcode) {
        this.tripcode = tripcode;
    }

    public String getTripname() {
        return tripname;
    }

    public void setTripname(String tripname) {
        this.tripname = tripname;
    }
}
