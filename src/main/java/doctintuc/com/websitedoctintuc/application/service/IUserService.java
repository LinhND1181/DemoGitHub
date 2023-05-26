package doctintuc.com.websitedoctintuc.application.service;

import doctintuc.com.websitedoctintuc.application.request.LoginRequest;
import doctintuc.com.websitedoctintuc.application.response.UserResponse;
import doctintuc.com.websitedoctintuc.domain.dto.UserDTO;
import doctintuc.com.websitedoctintuc.domain.entity.User;

import java.util.List;

public interface IUserService {

    User create(UserDTO accountDTO);

    User get(int id);

    User update(int id, UserDTO accountDTO);

    String delete(int id);

    List<User> searchAll(Integer page, Integer size);

    UserResponse login(LoginRequest loginRequest);

    User logout();



}