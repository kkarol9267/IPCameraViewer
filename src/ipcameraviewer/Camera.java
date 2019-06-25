package ipcameraviewer;

public class Camera {
    private String name;
    private String ip_address;
    private boolean state;

    public Camera(String name, String ip_address, boolean state) {
        this.name = name;
        this.ip_address = ip_address;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Camera{" + "name=" + name + ", ip_address=" + ip_address + ", state=" + state + '}';
    }
    
    
}
