package com.zyr.shop.service;

import com.zyr.shop.common.BusinessException;
import com.zyr.shop.common.PageResult;
import com.zyr.shop.dto.UserVO;
import com.zyr.shop.entity.SysUser;
import com.zyr.shop.repository.SysUserRepository;
import com.zyr.shop.util.Md5Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户管理服务（管理员后台使用）
 *
 * @author zyr
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserService {

    /** 用户数据访问 */
    private final SysUserRepository userRepository;

    /**
     * 分页查询用户列表
     *
     * @param keyword 用户名/昵称关键词
     * @param page    页码
     * @param size    每页条数
     * @return 用户分页结果
     */
    public PageResult<UserVO> page(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        // 简化：先用 findAll 然后内存过滤；用户量小，足够入门学习
        List<SysUser> all = userRepository.findAll();
        List<SysUser> filtered;
        if (keyword == null || keyword.isBlank()) {
            filtered = all;
        } else {
            String kw = keyword.toLowerCase();
            filtered = all.stream()
                    .filter(u -> (u.getUsername() != null && u.getUsername().toLowerCase().contains(kw))
                            || (u.getNickname() != null && u.getNickname().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }
        // 排序后分页
        filtered.sort((a, b) -> b.getId().compareTo(a.getId()));
        int total = filtered.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<UserVO> records = filtered.subList(from, to).stream().map(UserVO::from).toList();
        PageResult<UserVO> result = new PageResult<>();
        result.setPage((long) page);
        result.setSize((long) size);
        result.setTotal((long) total);
        result.setRecords(records);
        return result;
    }

    /**
     * 新增用户（管理员后台添加，默认密码 123456 的 MD5）
     *
     * @param username  用户名
     * @param nickname  昵称
     * @param role      角色
     * @param phone     手机号
     * @param address   收货地址
     * @return 新增后的用户视图
     */
    public UserVO create(String username, String nickname, String role, String phone, String address) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessException(400, "用户名已存在");
        }
        SysUser u = new SysUser();
        u.setUsername(username);
        u.setPassword(Md5Util.md5("123456"));
        u.setNickname(nickname);
        u.setRole(role == null ? "USER" : role);
        u.setPhone(phone);
        u.setAddress(address);
        u.setStatus(1);
        SysUser saved = userRepository.save(u);
        log.info("【用户管理】新增用户 id={} username={}", saved.getId(), saved.getUsername());
        return UserVO.from(saved);
    }

    /**
     * 修改用户信息（不修改密码）
     *
     * @param id       用户ID
     * @param nickname 昵称
     * @param role     角色
     * @param phone    手机号
     * @param address  收货地址
     * @param status   状态
     * @return 修改后的用户视图
     */
    public UserVO update(Long id, String nickname, String role, String phone, String address, Integer status) {
        SysUser u = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (nickname != null) u.setNickname(nickname);
        if (role != null) u.setRole(role);
        if (phone != null) u.setPhone(phone);
        if (address != null) u.setAddress(address);
        if (status != null) u.setStatus(status);
        SysUser saved = userRepository.save(u);
        log.info("【用户管理】修改用户 id={}", id);
        return UserVO.from(saved);
    }

    /**
     * 重置用户密码为 123456 的 MD5
     *
     * @param id 用户ID
     */
    public void resetPassword(Long id) {
        SysUser u = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        u.setPassword(Md5Util.md5("123456"));
        userRepository.save(u);
        log.info("【用户管理】重置用户密码 id={}", id);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(404, "用户不存在");
        }
        userRepository.deleteById(id);
        log.info("【用户管理】删除用户 id={}", id);
    }
}
