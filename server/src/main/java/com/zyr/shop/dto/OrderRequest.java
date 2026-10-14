package com.zyr.shop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建订单请求参数
 *
 * @author zyr
 */
@Data
public class OrderRequest {

    /** 收货人姓名 */
    @NotBlank(message = "收货人不能为空")
    private String receiver;

    /** 收货人手机号 */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /** 收货地址 */
    @NotBlank(message = "收货地址不能为空")
    private String address;

    /** 备注（用户留言，可空） */
    private String remark;
}
