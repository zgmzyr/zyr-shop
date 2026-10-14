package com.zyr.shop.dto;

import com.zyr.shop.entity.SysUser;
import lombok.Data;

/**
 * 用户视图对象（不暴露密码字段）
 *
 * <p>登录成功、获取当前用户信息时返回前端</p>
 *
 * @author zyr
 */
@Data
public class UserVO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像访问路径 */
    private String avatar;

    /** 角色：ADMIN / USER */
    private String role;

    /** 手机号 */
    private String phone;

    /** 收货地址 */
    private String address;

    /** 状态：1-正常，0-禁用 */
    private Integer status;

    /**
     * 由实体转换为视图对象
     *
     * @param user 用户实体
     * @return 视图对象
     */
    public static UserVO from(SysUser user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.id = user.getId();
        vo.username = user.getUsername();
        vo.nickname = user.getNickname();
        vo.avatar = user.getAvatar();
        vo.role = user.getRole();
        vo.phone = user.getPhone();
        vo.address = user.getAddress();
        vo.status = user.getStatus();
        return vo;
    }
}
