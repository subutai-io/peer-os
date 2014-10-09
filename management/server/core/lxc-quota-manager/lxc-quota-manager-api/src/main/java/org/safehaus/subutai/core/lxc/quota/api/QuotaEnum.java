package org.safehaus.subutai.core.lxc.quota.api;


/**
 * Created by talas on 10/7/14.
 */

public enum QuotaEnum
{
    /**
     * Controls and reports block I/O operations.
     */
    BLKIO_IO_MERGED( "blkio.io_merged" ),
    BLKIO_IO_QUEUED( "blkio.io_queued" ),
    BLKIO_IO_SERVICE_BYTES( "blkio.io_service_bytes" ),
    BLKIO_IO_SERVICED( "blkio.io_serviced" ),
    BLKIO_IO_SERVICE_TIME( "blkio.io_service_time" ),
    BLKIO_IO_WAIT_TIME( "blkio.io_wait_time" ),
    BLKIO_RESET_STATS( "blkio.reset_stats" ),
    BLKIO_SECTORS( "blkio.sectors" ),
    BLKIO_THROTTLE_IO_SERVICE_BYTES( "blkio.throttle.io_service_bytes" ),
    BLKIO_THROTTLE_IO_SERVICED( "blkio.throttle.io_serviced" ),
    BLKIO_THROTTLE_READ_BPS_DEVICE( "blkio.throttle.read_bps_device" ),
    BLKIO_THROTTLE_READ_IOPS_DEVICE( "blkio.throttle.read_iops_device" ),
    BLKIO_THROTTLE_WRITE_BPS_DEVICE( "blkio.throttle.write_bps_device" ),
    BLKIO_THROTTLE_WRITE_IOPS_DEVICE( "blkio.throttle.write_iops_device" ),
    BLKIO_TIME( "blkio.time" ),
    BLKIO_WEIGHT( "blkio.weight" ),
    BLKIO_WEIGHT_DEVICE( "blkio.weight_device" ),

    /**
     * Controls access to CPU resources.
     */
    CPU_RT_PERIOD_US( "cpu.rt_period_us" ),
    CPU_RT_RUNTIME_US( "cpu.rt_runtime.us" ),
    CPU_SHARES( "cpu.shares" ),

    /**
     * Reports usage of CPU resources.
     */
    CPUACCT_STAT( "cpuacct.stat" ),
    CPUACCT_USAGE( "cpuacct.usage" ),
    CPUACCT_USAGE_PERCPU( "cpuacct.usage_percpu" ),

    /**
     * Controls access to CPU cores and memory nodes.
     */
    CPUSET_CPU_EXCLUSIVE( "cpuset.cpu_exclusive" ),
    CPUSET_CPUS( "cpuset.cpus" ),
    CPUSET_MEM_EXCLUSIVE( "cpuset.mem_exclusive" ),
    CPUSET_MEM_HARDWALL( "cpuset.mem_hardwall" ),
    CPUSET_MEMORY_MIGRATE( "cpuset.memory_migrate" ),
    CPUSET_MEMORY_PRESSURE( "cpuset.memory_pressure" ),
    CPUSET_MEMORY_PRESSURE_ENABLED( "cpuset.memory_pressure_enabled" ),
    CPUSET_MEMORY_SPREAD_PAGE( "cpuset.memory_spread_page" ),
    CPUSET_MEMORY_SPREAD_SLAB( "cpuset.memory_spread_slab" ),
    CPUSET_MEMS( "cpuset.mems" ),
    CPUSET_SCHED_LOAD_BALANCE( "cpuset.sched_load_balance" ),
    CPUSET_SCHED_RELAX_DOMAIN_LEVEL( "cpuset.sched_relax_domain_level" ),

    /**
     * Controls access to system devices.
     */
    DEVICES_ALLOW( "devices.allow" ),
    DEVICES_DENY( "devices.deny" ),
    DEVICES_LIST( "devices.list" ),

    /**
     * Suspends or resumes cgroup tasks.
     */
    FREEZER_STATE( "freezer.state" ),

    /**
     * Controls access to memory resources, and reports on memory usage.
     */
    MEMORY_FAILCNT( "memory.failcnt" ),
    MEMORY_FORCE_EMPTY( "memory.force_empty" ),
    MEMORY_LIMIT_IN_BYTES( "memory.limit_in_bytes" ),
    MEMORY_MAX_USAGE_IN_BYTES( "memory.max_usage_in_bytes" ),
    MEMORY_MEMSW_FAILCNT( "memory.memsw.failcnt" ),
    MEMORY_MEMSW_LIMIT_IN_BYTES( "memory.memsw.limit_in_bytes" ),
    MEMORY_MEMSW_MAX_USAGE_IN_BYTES( "memory.memsw.max_usage_in_bytes" ),
    MEMORY_MEMSW_USAGE_IN_BYTES( "memory.memsw.usage_in_bytes" ),
    MEMORY_MOVE_CHARGE_AT_IMMIGRATE( "memory.move_charge_at_immigrate" ),
    MEMORY_NUMA_STAT( "memory.numa_stat" ),
    MEMORY_OOM_CONTROL( "memory.oom_control" ),
    MEMORY_SOFT_LIMIT_IN_BYTES( "memory.soft_limit_in_bytes" ),
    MEMORY_STAT( "memory.stat" ),
    MEMORY_SWAPPINESS( "memory.swappiness" ),
    MEMORY_USAGE_IN_BYTES( "memory.usage_in_bytes" ),
    MEMORY_USE_HIERARCHY( "memory.use_hierarchy" ),

    /**
     * Tags network packets for use by network traffic control.
     */
    NET_CLS_CLASSID( "net_cls.classid" );

    private String key;


    private QuotaEnum( String key )
    {
        this.key = key;
    }


    public String getKey()
    {
        return key;
    }
}
