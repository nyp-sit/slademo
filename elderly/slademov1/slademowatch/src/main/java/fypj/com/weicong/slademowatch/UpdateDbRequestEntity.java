package fypj.com.weicong.slademowatch;

/**
 * Created by L30911 on 12/17/2015.
 */
public class UpdateDbRequestEntity {
    private int id;
    private String path;
    private String args;
    private boolean stored;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public boolean isStored() {
        return stored;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }
}
