package com.is.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "ClockTime")
@Table(name = "clock_time")
public class ClockTime {
	
	@Id
	@Column(name = "cp_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int cpId;
	
	@Column(name = "clock_time")
	private String clockTime;
	
	@Column(name = "employee_id") 
	private String employeeId;
	
	
	@Column(name = "device_id")
	private String deviceId;
	
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public String getDeviceId() {
		return deviceId;
	}

	public int getCpId() {
		return cpId;
	}

	public void setCpId(int cpId) {
		this.cpId = cpId;
	}
	
	



	public String getClockTime() {
		return clockTime;
	}

	public void setClockTime(String clockTime) {
		this.clockTime = clockTime;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	
	public String getEmployeeId() {
		return employeeId;
	}

	
	

}
