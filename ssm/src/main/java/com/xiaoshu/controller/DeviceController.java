package com.xiaoshu.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.xiaoshu.config.util.ConfigUtil;
import com.xiaoshu.entity.Device;
import com.xiaoshu.entity.DeviceVo;
import com.xiaoshu.entity.Operation;
import com.xiaoshu.entity.Role;
import com.xiaoshu.entity.Type;
import com.xiaoshu.entity.User;
import com.xiaoshu.service.DeviceService;
import com.xiaoshu.service.OperationService;
import com.xiaoshu.service.RoleService;
import com.xiaoshu.service.UserService;
import com.xiaoshu.util.StringUtil;
import com.xiaoshu.util.WriterUtil;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;

@Controller
@RequestMapping("phone")
public class DeviceController extends LogController{
	static Logger logger = Logger.getLogger(DeviceController.class);

	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService ;
	
	@Autowired
	private OperationService operationService;
	
	@Autowired
	private DeviceService deviceService;
	
	@RequestMapping("phoneIndex")
	public String index(HttpServletRequest request,Integer menuid) throws Exception{
		List<Role> roleList = roleService.findRole(new Role());
		List<Operation> operationList = operationService.findOperationIdsByMenuid(menuid);
		request.setAttribute("operationList", operationList);
		request.setAttribute("roleList", roleList);
		List<Type> list = deviceService.findAllT();
		request.setAttribute("tlist", list);
		return "phone";
	}
	
	
	@RequestMapping(value="phoneList",method=RequestMethod.POST)
	public void phoneList(DeviceVo deviceVo,HttpServletRequest request,HttpServletResponse response,String offset,String limit) throws Exception{
		try {
			
			String order = request.getParameter("order");
			String ordername = request.getParameter("ordername");
			
			
			Integer pageSize = StringUtil.isEmpty(limit)?ConfigUtil.getPageSize():Integer.parseInt(limit);
			Integer pageNum =  (Integer.parseInt(offset)/pageSize)+1;
			//PageInfo<User> userList= userService.findUserPage(user,pageNum,pageSize,ordername,order);
			
			PageInfo<DeviceVo> page = deviceService.findPage(deviceVo, pageNum, pageSize);
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("total",page.getTotal() );
			jsonObj.put("rows", page.getList());
	        WriterUtil.write(response,jsonObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("用户展示错误",e);
			throw e;
		}
	}
	
	
	// 新增或修改
	@RequestMapping("reserveUser")
	public void reserveUser(Device device, HttpServletRequest request,User user,HttpServletResponse response){
		//Integer userId = user.getUserid();
		Integer id = device.getDeviceid();
		JSONObject result=new JSONObject();
		device.setCreatetime(new Date());
		try {
			Device device2 = deviceService.findByName(device.getDeviceName());
			if (id != null) {   // userId不为空 说明是修改
				//User userName = userService.existUserWithUserName(user.getUsername());
				if(device2==null||(device2 != null && device2.getDeviceid().equals(id))){
					//user.setUserid(userId);
					//userService.updateUser(user);
					deviceService.updateD(device);
					result.put("success", true);
				}else{
					result.put("success", true);
					result.put("errorMsg", "该设备名被使用");
				}
				
			}else {   // 添加
				if(device2==null){  // 没有重复可以添加
					//userService.addUser(user);
					deviceService.addD(device);
					result.put("success", true);
				} else {
					result.put("success", true);
					result.put("errorMsg", "该设备名被使用");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("保存用户信息错误",e);
			result.put("success", true);
			result.put("errorMsg", "对不起，操作失败");
		}
		WriterUtil.write(response, result.toString());
	}
	
	
	@RequestMapping("deleteUser")
	public void delUser(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		try {
			String[] ids=request.getParameter("ids").split(",");
			for (String id : ids) {
				//userService.deleteUser(Integer.parseInt(id));
				deviceService.delD(Integer.parseInt(id));
			}
			result.put("success", true);
			result.put("delNums", ids.length);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除用户信息错误",e);
			result.put("errorMsg", "对不起，删除失败");
		}
		WriterUtil.write(response, result.toString());
	}
	@RequestMapping("echartsPerson")
	public void echartsPerson(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		try {
		
			List<DeviceVo> data= deviceService.findE();
			result.put("success", true);
			
			result.put("data", data);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("表信息错误",e);
			result.put("errorMsg", "对不起，删除失败");
		}
		WriterUtil.write(response, result.toString());
	}
	@RequestMapping("editPassword")
	public void editPassword(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		String oldpassword = request.getParameter("oldpassword");
		String newpassword = request.getParameter("newpassword");
		HttpSession session = request.getSession();
		User currentUser = (User) session.getAttribute("currentUser");
		if(currentUser.getPassword().equals(oldpassword)){
			User user = new User();
			user.setUserid(currentUser.getUserid());
			user.setPassword(newpassword);
			try {
				userService.updateUser(user);
				currentUser.setPassword(newpassword);
				session.removeAttribute("currentUser"); 
				session.setAttribute("currentUser", currentUser);
				result.put("success", true);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("修改密码错误",e);
				result.put("errorMsg", "对不起，修改密码失败");
			}
		}else{
			logger.error(currentUser.getUsername()+"修改密码时原密码输入错误！");
			result.put("errorMsg", "对不起，原密码输入错误！");
		}
		WriterUtil.write(response, result.toString());
	}
}