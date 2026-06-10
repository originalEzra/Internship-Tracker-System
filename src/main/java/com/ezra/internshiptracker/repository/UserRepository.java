package com.ezra.internshiptracker.repository;

//Spring 自动帮你生成的数据库操作工具 以后不用controller写SQL原始语句 crud都在这写
//service也不直接操作数据库

import com.ezra.internshiptracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;



import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> { //
    Optional<User> findByUsername(String username); //查询用户名 校验password

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);
}
