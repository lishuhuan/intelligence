package com.is.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "ClockAbnormal")
@Table(name = "clock_abnormal")
public class ClockAbnormal {

	@Id
	@Column(name = "id")
	private String id;
	
	@Column(name = "clock_time")
	private String clockTime;
	
	@Column(name = "employee_id") 
	private String employeeId;
	
	
	@Column(name = "device_id")
	private String deviceId;
	
	@Column(name = "photo_path")
	private String photoPath;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClockTime() {
		return clockTime;
	}

	public void setClockTime(String clockTime) {
		this.clockTime = clockTime;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getPhotoPath() {
		return photoPath;
	}

	public void setPhotoPath(String photoPath) {
		this.photoPath = photoPath;
	}
	
	
}
