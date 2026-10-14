package com.zyr.shop.controller.admin;

import com.zyr.shop.common.PageResult;
import com.zyr.shop.common.Result;
import com.zyr.shop.dto.UserVO;
import com.zyr.shop.security.RequireAdmin;
import com.zyr.shop.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员用户管理接口（需 ADMIN 角色）
 *
 * @author zyr
 */
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    /** 用户管理服务 */
    private final SysUserService userService;

    /**
     * 用户分页查询
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @RequireAdmin
    @GetMapping("/list")
    public Result<PageResult<UserVO>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.page(keyword, page, size));
    }

    /**
     * 新增用户（默认密码 123456）
     *
     * @param username 用户名
     * @param nickname 昵称
     * @param role     角色
     * @param phone    手机号
     * @param address  收货地址
     * @return 新增后的用户
     */
    @RequireAdmin
    @PostMapping
    public Result<UserVO> create(@RequestParam String username,
                                 @RequestParam(required = false) String nickname,
                                 @RequestParam(defaultValue = "USER") String role,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam(required = false) String address) {
        return Result.success(userService.create(username, nickname, role, phone, address));
    }

    /**
     * 修改用户
     *
     * @param id       用户ID
     * @param nickname 昵称
     * @param role     角色
     * @param phone    手机号
     * @param address  收货地址
     * @param status   状态
     * @return 修改后的用户
     */
    @RequireAdmin
    @PutMapping("/{id}")
    public Result<UserVO> update(@PathVariable Long id,
                                 @RequestParam(required = false) String nickname,
                                 @RequestParam(required = false) String role,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam(required = false) String address,
                                 @RequestParam(required = false) Integer status) {
        return Result.success(userService.update(id, nickname, role, phone, address, status));
    }

    /**
     * 重置密码为 123456
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @RequireAdmin
    @PutMapping("/{id}/password/reset")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @RequireAdmin
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
