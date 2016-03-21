package io.subutai.core.environment.rest.ui.entity;

public class ResourceHostDto {
    private String id;
    private String cpu;
    private String cpuAvailable;
    private String memory;
    private String memoryAvailable;
    private String hdd;
    private String hddAvailable;

    public ResourceHostDto(String id, String cpu, String cpuAvailable, String memory, String memoryAvailable, String hdd, String hddAvailable) {
        this.id = id;
        this.cpu = cpu;
        this.cpuAvailable = cpuAvailable;
        this.memory = memory;
        this.memoryAvailable = memoryAvailable;
        this.hdd = hdd;
        this.hddAvailable = hddAvailable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getCpuAvailable() {
        return cpuAvailable;
    }

    public void setCpuAvailable(String cpuAvailable) {
        this.cpuAvailable = cpuAvailable;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getMemoryAvailable() {
        return memoryAvailable;
    }

    public void setMemoryAvailable(String memoryAvailable) {
        this.memoryAvailable = memoryAvailable;
    }

    public String getHdd() {
        return hdd;
    }

    public void setHdd(String hdd) {
        this.hdd = hdd;
    }

    public String getHddAvailable() {
        return hddAvailable;
    }

    public void setHddAvailable(String hddAvailable) {
        this.hddAvailable = hddAvailable;
    }
}
