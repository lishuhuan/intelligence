package com.is.dao.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.is.constant.Hql;
import com.is.dao.CloudDao;
import com.is.dao.IntelligenceDao;
import com.is.model.Admin;
import com.is.model.Appointment;
import com.is.model.ClockAbnormal;
import com.is.model.ClockAppeal;
import com.is.model.ClockRecord;
import com.is.model.ClockRecordDept;
import com.is.model.ClockRecordSelect;
import com.is.model.ClockTime;
import com.is.model.CollectionPhoto;
import com.is.model.Company;
import com.is.model.Department;
import com.is.model.Employee;
import com.is.model.EmployeeClock;
import com.is.model.EmployeeId;
import com.is.model.Message;
import com.is.model.Notification;
import com.is.model.Template;
import com.is.model.VersionUpdate;
import com.is.model.Visitor;
import com.is.model.VisitorInfo;
import com.is.util.Page;

import io.netty.util.internal.StringUtil;

/**
 * @author lishuhuan
 * @date 2016年3月31日 类说明
 */
@Repository("intelligenceDao")
public class IntelligenceDaoImpl implements IntelligenceDao {

	@Autowired
	private SessionFactory sessionFactory;

	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	@Autowired
	private CloudDao cloudDao;

	@Override
	public Admin getAdminByName(String username) {
		List<Admin> list = cloudDao.findByHql(Hql.GET_USER_BY_NAME, username);
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<Employee> getEmployeeList(int companyId) {
		return cloudDao.findByHql(Hql.GET_EMPLOYEE_LIST, companyId);
	}

	@Override
	public Company getCompanyById(int id) {
		return (Company) cloudDao.getByHql(Hql.GET_COMPANY_BY_ID, id);
	}

	@Override
	public List<Company> getCompanyList() {
		return cloudDao.findByHql(Hql.GET_COMPANY_LIST);
	}

	@Override
	public List<ClockRecord> getClockList() {
		return cloudDao.findByHql(Hql.GET_CLOCK_LIST);
	}

	@Override
	public List<ClockRecordSelect> getClockByWhere(String department, String user, String startClock, String endClock,
			String rule, String companyId) {
		String sql = "select c.*,b.employee_name as employeeName,b.job_id as jobId,d.department as department from clockrecord c LEFT JOIN employee b on c.employee_id=b.employee_id LEFT JOIN department d on d.id=b.department_id where b.company_id='"
				+ companyId + "'";
		Map<Integer, String> map = new HashMap<>();
		int i = 0;
		if (!StringUtil.isNullOrEmpty(user)) {
			sql = sql + " and (b.pingyin like ? or b.pingyin like ? or b.employee_name like ?)";
			map.put(i, user + '%');
			map.put(i + 1, '%' + "," + user + '%');
			map.put(i + 2, '%' + user + '%');
			i = i + 3;
		}
		if (!StringUtil.isNullOrEmpty(startClock)) {
			startClock = startClock + " 00:00:00";
			sql += " and c.start_clock>=?";
			map.put(i, startClock);
			i = i + 1;
		}
		if (!StringUtil.isNullOrEmpty(department)) {
			sql += " and b.department_id=?";
			map.put(i, department);
			i = i + 1;
		}
		if (!StringUtil.isNullOrEmpty(endClock)) {
			endClock = endClock + " 23:59:59";
			sql += " and c.start_clock<=?";
			map.put(i, endClock);
		}
		if ("1".equals(rule)) {
			sql += " and c.state!=0";
		}
		sql+=" order by c.start_clock";

		Query query = getSession().createSQLQuery(sql);
		for (int p = 0; p < map.size(); p++) {
			query.setParameter(p, map.get(p));
		}
		return ((SQLQuery) query).addEntity(ClockRecordSelect.class).list();
	}

	@Override
	public List<Employee> getEmployeeByWhere(String word, String department, int companyId) {
		String sql="select a from Employee a where a.company.companyId="+companyId;
		//不在员工页面展示管理员账号
		sql += " and a.admin.authority!=0";
		int i=0;
		Map<Integer, String> map = new HashMap<>();
		if (!StringUtil.isNullOrEmpty(department)) {
			if(department.equals("0")){
				sql = sql + " and a.department=null";
			}
			else{
				sql = sql + " and a.department.id=?";
				map.put(i, department);
				i = i + 1;
			}
		}
		if (!StringUtil.isNullOrEmpty(word)) {
			sql += " and (a.pingyin like ? or a.pingyin like ? or a.employeeName like ?)";
			map.put(i, word + "%");
			map.put(i + 1, "%" + "," + word + "%");
			map.put(i + 2, "%" + word + "%");
		}
		
		Query query = getSession().createQuery(sql);
		for (int p = 0; p < map.size(); p++) {
			query.setParameter(p, map.get(p));
		}
		
		return query.list();

	}

	@Override
	public List<Notification> getNotifyList() {
		return cloudDao.findByHql(Hql.GET_NOTIFY_LIST);
	}

	@Override
	public List<Appointment> getAppointList() {
		Query query = getSession()
				.createSQLQuery("SELECT a.* from appointment a,employee b where a.employee_id=b.employee_id");
		List<Appointment> list = ((SQLQuery) query).addEntity(Appointment.class).list();
		return list;
	}

	@Override
	public Employee getEmployeeByAdmin(String adminId) {
		Employee employee = (Employee) cloudDao.getByHql(Hql.GET_EMPLOYEE_BY_ADMIN_ID, adminId);
		return employee;
	}

	@Override
	public Employee getEmployeeById(String id) {
		return (Employee) cloudDao.getByHql(Hql.GET_EMPLOYEE_BY_ID, id);
	}

	@Override
	public Notification getNotifyById(int id) {
		return (Notification) cloudDao.getByHql(Hql.GET_NOTIFY_BY_ID, id);
	}

	@Override
	public Appointment getAppointById(String id) {
		return (Appointment) cloudDao.getByHql(Hql.GET_APPOINT_BY_ID, id);
	}

	@Override
	public List<Employee> getEmployeeByCompany(int id) {
		return cloudDao.findByHql(Hql.GET_EMPLOYEE_BY_COMPANY, id);
	}

	@Override
	public ClockRecord getClockByMc(String id, String morningClock) {
		Query query = getSession().createSQLQuery("select * from clockrecord a where a.employee_id=" + "'" + id + "'"
				+ " and a.start_clock like " + "'%" + morningClock + "%'");
		ClockRecord clockRecord = (ClockRecord) ((SQLQuery) query).addEntity(ClockRecord.class).uniqueResult();
		return clockRecord;
	}

	@Override
	public ClockRecord getClockByNc(int id, String nightClock) {
		Query query = getSession().createSQLQuery("select * from clockrecord a where a.employee_id=" + "'" + id + "'"
				+ " and a.end_clock like " + "'%" + nightClock + "%'");
		ClockRecord clockRecord = (ClockRecord) ((SQLQuery) query).addEntity(ClockRecord.class).uniqueResult();
		return clockRecord;
	}

	@Override
	public List<Employee> getEmployeeByName(String name, int companyId) {
		return cloudDao.findByHql(Hql.GET_EMPLOYEE_BY_NAME, name, companyId);
	}

	@Override
	public Admin getAdminById(String id) {
		Query query = getSession().createSQLQuery("select a.* from admin a where a.admin_id='" + id + "'");
		Admin admin = (Admin) ((SQLQuery) query).addEntity(Admin.class).uniqueResult();
		return admin;
	}

	@Override
	public String getEmployeeIdByCompany(int id) {
		Query query = getSession().createSQLQuery(
				"SELECT a.employee_id from employee a,company c where a.admin_id=c.admin_id and c.company_id=" + "'"
						+ id + "'");
		String employeeId = query.uniqueResult().toString();
		return employeeId;
	}

	@Override
	public List<ClockTime> getClockPhoto() {
		return cloudDao.findByHql(Hql.GET_CLOCK_PHOTO);
	}

	@Override
	public String getEmployeeByMobile(String mobile) {
		Query query = getSession().createSQLQuery(
				"select DISTINCT e.employee_name from employee e where e.telphone='" + mobile + "' LIMIT 1");
		String name = (String) ((SQLQuery) query).uniqueResult();
		return name;
	}

	@Override
	public Company getCompanyByEmployee(String id) {
		Query query = getSession().createSQLQuery(
				"SELECT a.* from company a LEFT JOIN employee b on b.company_id=a.company_id where b.employee_id='" + id
						+ "'");
		Company company = (Company) ((SQLQuery) query).addEntity(Company.class).uniqueResult();
		return company;
	}

	@Override
	public List<ClockRecord> getClockByEmployee(String id) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String start = sdf.format(new Date())+" 00:00:00";
		String time=start.replace(start.substring(8, 10), "01");
		Query query = getSession().createQuery(
				"SELECT a,b.employeeName,b.jobId,b.department.department from ClockRecord a,Employee b where b.employeeId=a.employeeId and a.employeeId='"+id+"' and a.startClock>='"+time+"'");
		return query.list();
	}

	@Override
	public VisitorInfo getVisitorInfoById(String id) {
		return (VisitorInfo) cloudDao.getByHql(Hql.GET_VISITORINFO_BY_ID, id);
	}

	@Override
	public List<Department> getDepartmentByCompany(String company) {
		return cloudDao.findByHql(Hql.GET_DEPARTMENT_BY_COMPANY, company);
	}

	@Override
	public List<Visitor> indexVisitor(String departmentId, String name, String startTime, String endTime,
			String deviceId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String sql = "select a from Visitor a where a.deviceId='" + deviceId + "' ";
		Map<Integer, Object> map = new HashMap<>();
		int i = 0;
		if (!StringUtil.isNullOrEmpty(departmentId)) {
			sql = sql + " and a.employee.department.id=?";
			map.put(i, departmentId);
			i = i + 1;
		}
		if (!StringUtil.isNullOrEmpty(name)) {
			sql += " and (a.employee.pingyin like ? or a.employee.pingyin like ? or a.employee.employeeName like ?)";
			map.put(i, name + "%");
			map.put(i + 1, "%" + "," + name + "%");
			map.put(i + 2, "%" + name + "%");
			i = i + 3;
		}
		if (!StringUtil.isNullOrEmpty(startTime)) {
			startTime=startTime+" 00:00:00";
			sql += " and a.startTime>=?";
			try {
				map.put(i, sdf.parse(startTime));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			i = i + 1;
		}
		if (!StringUtil.isNullOrEmpty(endTime)) {
			endTime=endTime+" 23:59:59";
			sql += " and a.startTime<=?";
			try {
				map.put(i, sdf.parse(endTime));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		Query query = getSession().createQuery(sql);
		for (int p = 0; p < map.size(); p++) {
			query.setParameter(p, map.get(p));
		}
		List<Visitor> list = query.list();
		return list;
	}

	@Override
	public List<Visitor> indexVisitorPath(String departmentId, String name, String startTime, String endTime,
			String deviceId, int firstResult) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		String sql = "select a.* from visitor a left join employee b on a.employee_id=b.employee_id where a.device_id='"
				+ deviceId + "' ";
		Map<Integer, Object> map = new HashMap<>();
		int i = 0;
		if (!StringUtil.isNullOrEmpty(departmentId)) {
			sql = sql + " and b.department_id=?";
			map.put(i, departmentId);
			i = i + 1;
		}
		if (!StringUtil.isNullOrEmpty(name)) {
			sql += " and (b.pingyin like ? or b.pingyin like ? or b.employee_name like ?)";
			map.put(i, name + "%");
			map.put(i + 1, "%" + "," + name + "%");
			map.put(i + 2, "%" + name + "%");
			i = i + 3;
		}
		if (!StringUtil.isNullOrEmpty(startTime)) {
			sql += " and a.start_time>=?";
			try {
				map.put(i, sdf.parse(startTime));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			i = i + 1;
		}
		if (!StringUtil.isNullOrEmpty(endTime)) {
			sql += " and a.start_time<=?";
			try {
				map.put(i, sdf.parse(endTime));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		sql += " order by a.start_time desc";
		Query query = getSession().createSQLQuery(sql);
		for (int p = 0; p < map.size(); p++) {
			query.setParameter(p, map.get(p));
		}
		query.setFirstResult(firstResult);
		query.setMaxResults(25);
		List<Visitor> list = ((SQLQuery) query).addEntity(Visitor.class).list();
		return list;
	}

	@Override
	public int indexVisitorCount(String departmentId, String name, String startTime, String endTime, String deviceId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		String sql = "select count(*) from visitor a left join employee b on a.employee_id=b.employee_id where a.device_id='"
				+ deviceId + "' ";
		Map<Integer, Object> map = new HashMap<>();
		int i = 0;
		if (!StringUtil.isNullOrEmpty(departmentId)) {
			sql = sql + " and b.department_id=?";
			map.put(i, departmentId);
			i = i + 1;
		}
		if (!StringUtil.isNullOrEmpty(name)) {
			sql += " and (b.pingyin like ? or b.pingyin like ? or b.employee_name like ?)";
			map.put(i, name + "%");
			map.put(i + 1, "%" + "," + name + "%");
			map.put(i + 2, "%" + name + "%");
			i = i + 3;
		}
		if (!StringUtil.isNullOrEmpty(startTime)) {
			sql += " and a.start_time>=?";
			try {
				map.put(i, sdf.parse(startTime));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			i = i + 1;
		}
		if (StringUtil.isNullOrEmpty(endTime)) {
			sql += " and a.start_time<=?";
			try {
				map.put(i, sdf.parse(endTime));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		Query query = getSession().createSQLQuery(sql);
		for (int p = 0; p < map.size(); p++) {
			query.setParameter(p, map.get(p));
		}
		Object count = query.uniqueResult();
		return Integer.parseInt(count.toString());
	}

	@Override
	public Department getDepartmentById(String id) {
		return (Department) cloudDao.getByHql(Hql.GET_DEPARTMENT_BY_ID, id);
	}

	@Override
	public List<Message> getMessageByEmployee(String employeeId) {
		return cloudDao.findByHql(Hql.GET_MESSAGE_BY_EMPLOYEE_ID, employeeId);
	}

	@Override
	public Company getCompanyByDeviceId(String deviceId) {
		Query query = getSession().createSQLQuery("select a.* from company a LEFT JOIN device b on b.company_id=a.company_id where b.device_id='"+deviceId+"'");
		Company company =  (Company) ((SQLQuery) query).addEntity(Company.class).uniqueResult();
		return company;
	}

	@Override
	public List<Admin> searchAdmin(String name, String auth, String deviceId) {
		String sql = "select a.admin,a.employeeName from Employee a where a.admin.deviceId='" + deviceId + "'";
		Map<Integer, Object> map = new HashMap<>();
		int i = 0;
		if (!StringUtil.isNullOrEmpty(name)) {
			sql = sql + " and a.employeeName like ?";
			map.put(i, '%' + name + '%');
			i = i + 1;
		}
		if (!StringUtil.isNullOrEmpty(auth)) {
			sql += " and a.admin.authority=?";
			map.put(i, Integer.parseInt(auth));
		}

		Query query = getSession().createQuery(sql);
		for (int p = 0; p < map.size(); p++) {
			query.setParameter(p, map.get(p));
		}
		List<Admin> list = query.list();
		return list;
	}

	@Override
	public Visitor getVisitorById(String id) {
		Visitor visitor = (Visitor) cloudDao.getByHql(Hql.GET_VISITOR_BY_ID, id);
		return visitor;
	}

	@Override
	public List<Employee> getAuditPersonList(String deviceId) {
		Query query = getSession().createSQLQuery(
				"select a.* from employee a LEFT JOIN admin b on b.admin_id=a.admin_id where b.device_id='" + deviceId
						+ "' and b.audit_auth=1");
		List<Employee> list = ((SQLQuery) query).addEntity(Employee.class).list();
		return list;
	}

	@Override
	public List<Department> getDepartmentByGrade(String companyId, int grade) {
		return cloudDao.findByHql(Hql.GET_DEPARTMENT_BY_GRADE, companyId, grade);
	}

	@Override
	public List<ClockAppeal> getClockTimeAppealByEmployee(String employeeId) {
		return cloudDao.findByHql(Hql.GET_CLOCK_APPEAL_BY_EMPLOYEE, employeeId);
	}

	@Override
	public ClockAppeal getClockAppealById(String id) {
		return (ClockAppeal) cloudDao.getByHql(Hql.GET_CLOCK_APPEAL_BY_ID, id);
	}

	@Override
	public Map<String, String> getDepartmentOrganization(String departmentId, String grade) {
		String sql = null;
		if (grade.equals("1")) {
			sql = "select a.department as one from department a where a.id='" + departmentId + "'";
		} else if (grade.equals("2")) {
			sql = "select a.department as one,b.department as two from department a,department b where b.parent_id=a.id and b.id='"
					+ departmentId + "'";
		} else if (grade.equals("3")) {
			sql = "select a.department as one,b.department as two,c.department as three from department a,department b,department c where b.parent_id=a.id and c.parent_id=b.id and c.id='"
					+ departmentId + "'";
		} else if (grade.equals("4")) {
			sql = "select a.department as one,b.department as two,c.department as three,d.department as four from department a,department b,department c,department d where b.parent_id=a.id and c.parent_id=b.id and d.parent_id=c.id and d.id='"
					+ departmentId + "'";
		} else {
			sql = "select a.department as one,b.department as two,c.department as three,d.department as four,e.department as five from department a,department b,department c,department d,department e where b.parent_id=a.id and c.parent_id=b.id and d.parent_id=c.id and e.parent_id=d.id and e.id='"
					+ departmentId + "'";
		}
		Query query = getSession().createSQLQuery(sql);
		return (Map) query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult();
	}

	@Override
	public List<ClockAppeal> getClockAuditList(String auditId) {
		return cloudDao.findByHql(Hql.GET_CLOCK_APPEAL_BY_AUDIT, auditId);
	}

	@Override
	public List<ClockTime> getDetailClock(String employeeId,String time) {
		String sql = "SELECT a,b.employeeName,b.jobId,b.department.department from ClockTime a,Employee b where b.employeeId=a.employeeId and a.employeeId=? and a.clockTime like ? order by a.clockTime";
		Query query = getSession().createQuery(sql);
		query.setParameter(0, employeeId);
		query.setParameter(1, '%' + time + '%');

		List<ClockTime> list = query.list();
		return list;
	}

	@Override
	public List<Visitor> getVisitorAll() {
		return cloudDao.findByHql(Hql.GET_VISITOR_ALL);
	}

	@Override
	public List<ClockAbnormal> getHandClockList(String startTime, String endTime, String deviceId) {
		String sql = "select a,b.employeeName from ClockAbnormal a,Employee b where a.employeeId=b.employeeId and a.deviceId='"
				+ deviceId + "'";
		Map<Integer, String> map = new HashMap<>();
		int i = 0;
		if (!StringUtil.isNullOrEmpty(startTime)) {
			startTime=startTime+" 00:00:00";
			sql += " and a.clockTime>=?";
			map.put(i, startTime);
			i = i + 1;
		}
		if (!StringUtil.isNullOrEmpty(endTime)) {
			endTime=endTime+" 23:59:59";
			sql += " and a.clockTime<=?";
			map.put(i, endTime);
		}

		Query query = getSession().createQuery(sql);
		for (int p = 0; p < map.size(); p++) {
			query.setParameter(p, map.get(p));
		}
		return query.list();
	}

	@Override
	public ClockAbnormal getHandClockById(String id) {
		return (ClockAbnormal) cloudDao.getByHql(Hql.GET_CLOCK_ABNORMAL_BY_ID, id);
	}

	@Override
	public List<VisitorInfo> getVisitorInfoByWhere(String companyId, String name) {
		if (!StringUtil.isNullOrEmpty(name)) {
			return cloudDao.findByHql(Hql.GET_VISITOR_INFO_LIST, companyId);
		} else {
			return cloudDao.findByHql(Hql.GET_VISITOR_INFO_BY_NAME, companyId, name);
		}
	}

	@Override
	public Page getCollectionPhotoList(String startTime, String endTime, String tag, String deviceId) {
		Page page=new Page();
		String sql = "select * from collection_photo where device_id='"
				+ deviceId + "' ";
		Map<Integer, Object> map = new HashMap<>();
		int i = 0;
		if (!StringUtil.isNullOrEmpty(startTime)) {
			sql += " and time>=?";
			map.put(i, startTime);
			i = i + 1;
		}
		if (!StringUtil.isNullOrEmpty(endTime)) {
			endTime=endTime+" 23:59:59";
			sql += " and time<=?";
			map.put(i, endTime);
		}
		sql += " order by time desc";
		Query query = getSession().createSQLQuery(sql);
		for (int p = 0; p < map.size(); p++) {
			query.setParameter(p, map.get(p));
		}
		int count=query.list().size();
		page.setPageTotal(count/25+1);
		
		query.setFirstResult((Integer.parseInt(tag)-1)*25);
		query.setMaxResults(25);
		List<CollectionPhoto> list = ((SQLQuery) query).addEntity(CollectionPhoto.class).list();
		page.setList(list);
		return page;
	}

	@Override
	public CollectionPhoto getCollectionPhotoById(String id) {
		return (CollectionPhoto) cloudDao.getByHql(Hql.GET_COLLECTION_PHOTO_BY_ID, id);
	}

	@Override
	public CollectionPhoto getCollectByStrangerId(String id) {
		return (CollectionPhoto) cloudDao.getByHql(Hql.GET_COLLECTION_BY_STRANGER_ID, id);
	}

	@Override
	public VersionUpdate autoUpdate(String deviceId) {
		Query query = getSession().createSQLQuery("select * from version_update where device_id IS NULL OR "
				+ "device_id ='" + deviceId +"' ORDER BY created_at DESC LIMIT 1");
		VersionUpdate update =  (VersionUpdate) ((SQLQuery) query).addEntity(VersionUpdate.class).uniqueResult();
		return update;
	}

	@Override
	public List<Appointment> getAppointmentByUser(String userid) {
		return cloudDao.findByHql(Hql.GET_APPOINTMENT_BY_USER, userid);
	}

	@Override
	public Employee getEmployeeByPhotoPath(String path) {
		return (Employee) cloudDao.getByHql(Hql.GET_EMPLOYEE_BY_PHOTO_PATH, path);
	}

	@Override
	public List<Appointment> getAppointmentByVisitor(String visitorId) {
		return cloudDao.findByHql(Hql.GET_APPOINTMENT_BY_VISITOR, visitorId);
	}

	@Override
	public List<String> getDeviceList(String deviceId) {
		Query query = getSession().createSQLQuery("select a.device_id from device a LEFT JOIN device b on a.company_id=b.company_id where b.device_id='"+deviceId+"' and a.device_id!='"+deviceId+"'");
		List<String> result=query.list();
		return result;
	}
	
	@Override
	public List<String> getDeviceListAll(String deviceId) {
		Query query = getSession().createSQLQuery("select a.device_id from device a LEFT JOIN device b on a.company_id=b.company_id where b.device_id='"+deviceId+"'");
		List<String> result=query.list();
		return result;
	}

	@Override
	public String getCompanyIdByDeviceId(String deviceId) {
		Query query = getSession().createSQLQuery(
				"select company_id from device where device_id='"+deviceId+"'");
		String companyId = query.uniqueResult().toString();
		return companyId;
	}
	
	@Override
	public List<Employee> getEmployeeListByDeviceId(String deviceId) {
		Query query = getSession().createSQLQuery("select e.* from employee e LEFT JOIN device d on d.company_id=e.company_id where d.device_id='"+deviceId+"'");
		List<Employee> list = ((SQLQuery) query).addEntity(Employee.class).list();
		return list;
	}

	@Override
	public List<VisitorInfo> getVisitorByIds(String visitorIds,String deviceId) {
		Query query=null;
		if(!StringUtil.isNullOrEmpty(visitorIds)){
			query = getSession().createSQLQuery("select a.* from visitor_info a LEFT JOIN device b on b.company_id=a.company_id where b.device_id='"+deviceId+"' and a.id not in "+visitorIds);
		}
		else{
			query = getSession().createSQLQuery("select a.* from visitor_info a LEFT JOIN device b on b.company_id=a.company_id where b.device_id='"+deviceId+"'");
		}
		List<VisitorInfo> list = ((SQLQuery) query).addEntity(VisitorInfo.class).list();
		return list;
	}

	@Override
	public List<String> getExistVisitor(String visitorIds, String deviceId) {
		Query query = getSession().createSQLQuery("select a.id from visitor_info a LEFT JOIN device b on b.company_id=a.company_id where b.device_id='"+deviceId+"' and a.id not in "+visitorIds);
		List<String> list = query.list();
		return list;
	}

	@Override
	public List<ClockRecordDept> getClockByDepartmentData(String COMPANY_ID,String departmentId, String date) {
		//获取公司考勤规则
		Query company = getSession().createSQLQuery("select * from company where company_id='"+COMPANY_ID+"'");
		List<Company> rules = ((SQLQuery) company).addEntity(Company.class).list();
		if( rules.size() > 0 ){
		Company cc = rules.get(0);
		String morning_start = cc.getMorningTimeStart();
		String morning_end = cc.getMorningTimeEnd();
		String night_start = cc.getNightTimeStart();
		//获取考勤记录
		Query query = getSession().createSQLQuery(" select * from clockrecord where start_clock like '"+date+"%'");
		List<ClockRecord> list = ((SQLQuery) query).addEntity(ClockRecord.class).list();
		//获取人员名单
		Query employee = getSession().createSQLQuery(" select * from department as a,employee as b where a.company_id='"+COMPANY_ID+"' and a.id = b.department_id and b.department_id='"+departmentId+"'");
		List<ClockRecordDept> result = ((SQLQuery) employee).addEntity(ClockRecordDept.class).list();
		for (ClockRecordDept CRD : result) {
			String employee_id = CRD.employee_id();
			for (ClockRecord CR : list) {
				String e_id = CR.getEmployeeId();
				if( e_id.equals(employee_id) ){
					CRD.setTotal(CRD.getTotal()+1);
					//判断是是否早退
					int s = checkstate(morning_start,morning_end,night_start,CR.getStartClock(),CR.getEndClock());
					if( s==3 ){
						CRD.setEarly(CRD.getEarly()+1);
					}else if( s==2 ){
						CRD.setLate(CRD.getLate()+1);
					}
				}
			}
		}
		return result;
		
		
	}else{
		return null;
	}
	
	}

	private int checkstate(String morning_start, String morning_end,
			String night_start, String startClock, String endClock) {
		SimpleDateFormat simpleDateFormat =new SimpleDateFormat("HH:mm:ss");
		try {
			if( null != endClock && null != night_start && null != endClock){
				long date_morning_start=simpleDateFormat.parse(morning_start).getTime();
				long date_morning_end=simpleDateFormat.parse(morning_end).getTime();
				long date_night_start=simpleDateFormat.parse(night_start).getTime();
				
				long _startClock=simpleDateFormat.parse(startClock.substring(11, 19)).getTime();
				long _endClock=simpleDateFormat.parse(endClock.substring(11, 19)).getTime();
				//判断是否正常上班
				if( _startClock <= date_morning_end && _endClock >= _startClock && (_endClock-_startClock ) > ( date_night_start-date_morning_start ) ){
					return 1;
				}else if( _startClock > date_morning_end ){
					return 2;
				}else{
					return 3;
				}
			}else{
				//早退
				return 3;
			}
			
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		
		return 0;
	}

	@Override
	public List<EmployeeClock> getClockByEmployeeData(String employee_id,String company_id,String date) {
		//获取公司考勤规则
		Query company = getSession().createSQLQuery("select * from company where company_id='"+company_id+"'");
		List<Company> rules = ((SQLQuery) company).addEntity(Company.class).list();
		if( rules.size() > 0 ){
			Company cc = rules.get(0);
			String morning_start = cc.getMorningTimeStart();
			String morning_end = cc.getMorningTimeEnd();
			String night_start = cc.getNightTimeStart();	
			//获取到这个人的当月考勤记录
			Query query = getSession().createSQLQuery(" select * from clockrecord where start_clock like '"+date+"%' and employee_id='"+employee_id+"'");
			List<ClockRecord> list = ((SQLQuery) query).addEntity(ClockRecord.class).list();
			//获取人员信息
			Query employee = getSession().createSQLQuery("select a.employee_id,a.employee_name,b.department from employee as a,department as b where a.department_id = b.id and a.employee_id ='"+employee_id+"'");
			List<EmployeeClock> employeeresult = ((SQLQuery) employee).addEntity(EmployeeClock.class).list();
			if( employeeresult.size()>0 ){
				EmployeeClock empl = employeeresult.get(0);
				for (ClockRecord CRD : list) {
					empl.setTotal(empl.getTotal()+1);
					empl.setTotal_days(empl.getTotal_days()+","+CRD.getStartClock().substring(5,10));
					//判断是是否早退
					int s = checkstate(morning_start,morning_end,night_start,CRD.getStartClock(),CRD.getEndClock());
					if( s == 3 ){
						empl.setEarly(empl.getEarly()+1);
						empl.setEarly_days(empl.getEarly_days()+","+CRD.getStartClock().substring(5,10));
					}else if( s == 2 ){
						empl.setLate(empl.getLate()+1);
						empl.setLate_days(empl.getLate_days()+","+CRD.getStartClock().substring(5,10));
					}
				}
				return employeeresult;
				
			}
		}
		return null;
	}

	@Override
	public List<EmployeeClock> getClockByEmployeeDataKey(String key,String company_id, String date) {
		//获取人员信息
		Query employee = getSession().createSQLQuery("select employee_id from employee where pingyin like '%"+key+"%'");
		List<EmployeeId> employeeresult = ((SQLQuery) employee).addEntity(EmployeeId.class).list();
		List<EmployeeClock> result = new ArrayList<EmployeeClock>();
		for (EmployeeId EmployeeId : employeeresult) {
			String employee_id = EmployeeId.getEmployee_id();
			List<EmployeeClock> employ = this.getClockByEmployeeData(employee_id, company_id, date);
			if( employ!=null && employ.size()>0 ){
				result.add(employ.get(0));
			}
		}
		return result;
	}

	@Override
	public HSSFWorkbook export(List<Map<String, String>> list) {
		String[] excelHeader = { "Sno", "Name", "Age"};
		HSSFWorkbook wb = new HSSFWorkbook();    
        HSSFSheet sheet = wb.createSheet("Campaign");    
        HSSFRow row = sheet.createRow((int) 0);    
        HSSFCellStyle style = wb.createCellStyle();    
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		
        for (int i = 0; i < excelHeader.length; i++) {    
            HSSFCell cell = row.createCell(i);    
            cell.setCellValue(excelHeader[i]);    
            cell.setCellStyle(style);    
            sheet.autoSizeColumn(i);    
        } 
		return wb;
	}

	@Override
	public Template getTemplateById(String templateId) {
		return (Template) cloudDao.getByHql(Hql.GET_TEMPLATE_BY_ID, templateId);
	}

	@Override
	public Template getTemplateByPhotoPath(String path) {
		return (Template) cloudDao.getByHql(Hql.GET_TEMPLATE_BY_PHOTO_PATH, path);
	}

	@Override
	public List<Template> getTemplateListByEmployeeId(String employeeId) {
		return cloudDao.findByHql(Hql.GET_TEMPLATES_BY_EMPLOYEE_ID, employeeId);
	}

}
