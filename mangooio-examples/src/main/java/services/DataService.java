package services;

import io.mangoo.utils.CodecUtils;
import models.User;

public class DataService {
	public User getUser(String username) {
		return new User("admin", CodecUtils.hexJBcrypt("admin"));
	}
}