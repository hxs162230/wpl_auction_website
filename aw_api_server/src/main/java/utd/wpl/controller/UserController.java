package utd.wpl.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import utd.wpl.pojo.User;
import utd.wpl.service.UserService;
import com.mysql.jdbc.StringUtils;

@RestController
@RequestMapping("/user")
public class UserController {
	class Result {
		String answer;
		public String getAnswer() {
			return answer;
		}
		public void setAnswer(String answer) {
			this.answer = answer;
		}
		public Result() {
			// TODO Auto-generated constructor stub
			super();
		}
	}
	
	@Autowired
	private UserService userService;
	// 处理登录请求
	@GetMapping(value = "/{username}")
	public ResponseEntity<User> userProfile(@PathVariable("username") String un) {
		User findUser = userService.findUser(un);
		if (findUser == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(findUser, HttpStatus.OK);
	}
	
	
	@PostMapping(value = "/login")
	public ResponseEntity<Result> login(@RequestParam("username") String un, @RequestParam("password") String pswd) {
		User findUser = userService.findUser(un);
		String passwordMD5 = DigestUtils.md5DigestAsHex(pswd.getBytes());
		Result lResult = new Result();
		if (findUser == null) {
			lResult.setAnswer("User does not exist");
			return new ResponseEntity<>(lResult, HttpStatus.NOT_FOUND);
		}
		if (!passwordMD5.equals(findUser.getPassword())) {
			lResult.setAnswer("Password is incorrect");
			return new ResponseEntity<>(lResult, HttpStatus.NO_CONTENT);
		}
		findUser.setLast_visit(new Date());
		userService.updateLastVisit(findUser);
		lResult.setAnswer("Success");
		return new ResponseEntity<>(lResult, HttpStatus.OK);
		
	}

	// 用户注册
	@PostMapping(value = "/register")
	public ResponseEntity<Result> register(RequestEntity<User> requestEntity, @RequestParam("confirmPass") String cfn) {
		// 查询用户是否存在
		User user = requestEntity.getBody();
		User findUser = userService.findUser(user.getUsername());
		Result result = new Result();
		if (findUser != null) {
			result.setAnswer("User already exists");
			return new ResponseEntity<>(result, HttpStatus.FOUND);
		}
		if (!cfn.equals(user.getPassword())) {
			result.setAnswer("Password does not match");
			return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
		}
		//补全pojo属性
		user.setLast_visit(new Date());
		//密码使用MD5加密
		user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
		//添加新用户
		userService.addUser(user);
		result.setAnswer("Success");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
