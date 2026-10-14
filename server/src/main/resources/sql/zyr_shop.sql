-- ===================================================================
-- zyr_shop 商城系统 数据库建表与测试数据脚本
-- 数据库：MySQL 8，端口 3306，库名 zyr_shop
-- 默认密码均为 123456，MD5 后为 e10adc3949ba59abbe56e057f20f883e
-- @author zyr
-- 使用方式：先执行 `CREATE DATABASE zyr_shop DEFAULT CHARSET utf8mb4;`
--           再 `USE zyr_shop;` 然后执行本脚本
-- ===================================================================

DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS kb_chunk;
DROP TABLE IF EXISTS kb_document;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS shop_order;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS sys_user;

-- ---------------- 1. 系统用户表 ----------------
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username    VARCHAR(50) NOT NULL COMMENT '登录用户名',
    password    VARCHAR(64) NOT NULL COMMENT '登录密码（MD5）',
    nickname    VARCHAR(50)            COMMENT '昵称',
    avatar      VARCHAR(255)           COMMENT '头像路径',
    role        VARCHAR(20) NOT NULL COMMENT '角色 ADMIN/USER',
    phone       VARCHAR(20)            COMMENT '手机号',
    address     VARCHAR(255)           COMMENT '收货地址',
    status      INT          NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
    create_time DATETIME     NOT NULL COMMENT '创建时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ---------------- 2. 商品分类表 ----------------
CREATE TABLE category (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort        INT                   DEFAULT 0 COMMENT '排序值',
    status      INT          NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME     NOT NULL COMMENT '创建时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ---------------- 3. 商品表 ----------------
CREATE TABLE product (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    category_id BIGINT        NOT NULL COMMENT '分类ID',
    name        VARCHAR(100) NOT NULL COMMENT '商品名称',
    description VARCHAR(1000)          COMMENT '商品描述',
    price       DECIMAL(10,2) NOT NULL COMMENT '价格',
    stock       INT           NOT NULL DEFAULT 0 COMMENT '库存',
    sales       INT                    DEFAULT 0 COMMENT '销量',
    cover_image VARCHAR(500)           COMMENT '封面图片',
    status      INT           NOT NULL DEFAULT 1 COMMENT '状态 1上架 0下架',
    create_time DATETIME      NOT NULL COMMENT '创建时间',
    update_time DATETIME      NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ---------------- 4. 购物车表（每用户一辆） ----------------
CREATE TABLE cart (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT   NOT NULL COMMENT '用户ID',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_cart (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ---------------- 5. 购物车项表 ----------------
CREATE TABLE cart_item (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    cart_id      BIGINT        NOT NULL COMMENT '购物车ID',
    product_id   BIGINT        NOT NULL COMMENT '商品ID',
    price        DECIMAL(10,2) NOT NULL COMMENT '加入时单价快照',
    quantity     INT           NOT NULL DEFAULT 1 COMMENT '数量',
    create_time  DATETIME      NOT NULL COMMENT '加入时间',
    PRIMARY KEY (id),
    KEY idx_cart (cart_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车项表';

-- ---------------- 6. 订单表 ----------------
CREATE TABLE shop_order (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no     VARCHAR(32)   NOT NULL COMMENT '订单号',
    user_id      BIGINT        NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status       VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/PAID/SHIPPED/COMPLETED/CANCELLED',
    address      VARCHAR(500)            COMMENT '收货地址',
    phone        VARCHAR(20)             COMMENT '收货人手机',
    receiver     VARCHAR(50)             COMMENT '收货人姓名',
    remark       VARCHAR(500)            COMMENT '用户留言',
    create_time  DATETIME      NOT NULL COMMENT '创建时间',
    paid_at      DATETIME               COMMENT '支付时间',
    update_time  DATETIME      NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ---------------- 7. 订单项表 ----------------
CREATE TABLE order_item (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_id     BIGINT        NOT NULL COMMENT '订单ID',
    product_id   BIGINT        NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100)  NOT NULL COMMENT '商品名快照',
    price        DECIMAL(10,2) NOT NULL COMMENT '下单单价快照',
    quantity     INT           NOT NULL COMMENT '购买数量',
    create_time  DATETIME      NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- ---------------- 8. 支付记录表 ----------------
CREATE TABLE payment (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no    VARCHAR(32)   NOT NULL COMMENT '业务订单号',
    user_id     BIGINT        NOT NULL COMMENT '支付用户ID',
    amount      DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    pay_type    VARCHAR(20)   NOT NULL DEFAULT 'ALIPAY' COMMENT '支付方式',
    trade_no    VARCHAR(64)            COMMENT '支付宝交易号',
    buyer_id    VARCHAR(32)             COMMENT '买家支付宝ID',
    status      VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/SUCCESS/FAILED',
    paid_at     DATETIME                COMMENT '支付完成时间',
    raw_notify  TEXT                    COMMENT '异步通知原文',
    create_time DATETIME      NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- ---------------- 9. 知识库文档表 ----------------
CREATE TABLE kb_document (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    file_name   VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path   VARCHAR(500) NOT NULL COMMENT '磁盘绝对路径',
    file_url    VARCHAR(500)          COMMENT '访问URL',
    file_type   VARCHAR(20)  NOT NULL COMMENT '文件类型 TXT/DOC/PDF/MD',
    file_size   BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    chunk_count INT          NOT NULL DEFAULT 0 COMMENT '分块数',
    status      VARCHAR(20)  NOT NULL DEFAULT 'PROCESSING' COMMENT '状态 PROCESSING/READY/FAILED',
    uploader_id BIGINT                COMMENT '上传人ID',
    create_time DATETIME     NOT NULL COMMENT '创建时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- ---------------- 10. 知识库分块表 ----------------
CREATE TABLE kb_chunk (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    document_id BIGINT      NOT NULL COMMENT '文档ID',
    chunk_no    INT         NOT NULL COMMENT '分块序号',
    vector_id   VARCHAR(64) NOT NULL COMMENT 'Redis向量ID',
    content     TEXT        NOT NULL COMMENT '分块内容',
    create_time DATETIME    NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_doc (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库分块表';

-- ---------------- 11. 客服聊天消息表 ----------------
CREATE TABLE chat_message (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT      NOT NULL COMMENT '用户ID',
    role        VARCHAR(20) NOT NULL COMMENT '角色 user/assistant',
    content     TEXT        NOT NULL COMMENT '消息内容',
    create_time DATETIME    NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服聊天消息表';


-- ===================================================================
-- 测试数据
-- ===================================================================

-- ---------------- 用户：1 个管理员 + 2 个普通用户，密码均为 123456 ----------------
INSERT INTO sys_user(username, password, nickname, role, phone, address, status, create_time, update_time) VALUES
('admin',    'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN', '13800000000', '北京市海淀区中关村大街1号', 1, NOW(), NOW()),
('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三',       'USER',  '13800000001', '上海市浦东新区世纪大道100号', 1, NOW(), NOW()),
('lisi',     'e10adc3949ba59abbe56e057f20f883e', '李四',       'USER',  '13800000002', '广州市天河区体育西路89号',   1, NOW(), NOW());

-- ---------------- 商品分类 ----------------
INSERT INTO category(name, sort, status, create_time, update_time) VALUES
('数码电器', 1, 1, NOW(), NOW()),
('服装鞋帽', 2, 1, NOW(), NOW()),
('食品饮料', 3, 1, NOW(), NOW());

-- ---------------- 商品（8 个，封面图片用占位说明，实际可上传后替换） ----------------
INSERT INTO product(category_id, name, description, price, stock, sales, cover_image, status, create_time, update_time) VALUES
(1, 'Redmi Note 13 Pro', '6.67英寸 OLED屏，2亿像素主摄，67W快充，5000mAh大电池', 1399.00, 50, 120, '', 1, NOW(), NOW()),
(1, '小米手环8',         '1.62英寸 AMOLED屏，150+运动模式，14天续航',                 249.00, 200, 540, '', 1, NOW(), NOW()),
(1, '罗技G304无线鼠标',   'LIGHTSPEED无线技术，12000DPI，单节电池续航250小时',          149.00, 100, 88, '', 1, NOW(), NOW()),
(2, '优衣库 摇粒绒外套',  '修身版型，柔软保暖，多色可选，男女同款',                       199.00, 80,  66, '', 1, NOW(), NOW()),
(2, '安踏 跑步鞋',       '云感中底，透气网面，轻量缓震，适合日常通勤与慢跑',                299.00, 60,  42, '', 1, NOW(), NOW()),
(2, '南极人 纯棉T恤',     '100%新疆长绒棉，亲肤透气，多色多码可选',                         59.00, 300, 210, '', 1, NOW(), NOW()),
(3, '三只松鼠 坚果礼盒',   '每日坚果混合装，30小包独立锁鲜，办公室零食好选择',                89.00, 150, 180, '', 1, NOW(), NOW()),
(3, '元气森林 气泡水',    '0糖0脂0卡，白桃口味，500ml*15瓶整箱',                            69.00, 200, 95,  '', 1, NOW(), NOW());

-- ---------------- 购物车（zhangsan 与 lisi 各一辆） ----------------
INSERT INTO cart(user_id, create_time) VALUES
(2, NOW()),
(3, NOW());

-- ---------------- 购物车项（zhangsan 购物车有2个商品） ----------------
INSERT INTO cart_item(cart_id, product_id, price, quantity, create_time) VALUES
(1, 1, 1399.00, 1, NOW()),  -- Redmi Note 13 Pro x1
(1, 2, 249.00,  2, NOW());  -- 小米手环8 x2

-- ---------------- 订单（zhangsan 一笔已支付、一笔待支付；lisi 一笔已完成） ----------------
INSERT INTO shop_order(order_no, user_id, total_amount, status, address, phone, receiver, remark, create_time, paid_at, update_time) VALUES
('20261008000101', 2,  1897.00, 'PAID',     '上海市浦东新区世纪大道100号', '13800000001', '张三', '尽快发货', NOW(), NOW(), NOW()),
('20261008000102', 2,  149.00,  'PENDING',  '上海市浦东新区世纪大道100号', '13800000001', '张三', NULL,      NOW(), NULL,    NOW()),
('20261007000103', 3,  358.00,  'COMPLETED', '广州市天河区体育西路89号',   '13800000002', '李四', NULL,      NOW(), NOW(), NOW());

-- ---------------- 订单项 ----------------
INSERT INTO order_item(order_id, product_id, product_name, price, quantity, create_time) VALUES
(1, 1, 'Redmi Note 13 Pro', 1399.00, 1, NOW()),   -- 订单1
(1, 2, '小米手环8',         249.00,  2, NOW()),   -- 订单1
(2, 3, '罗技G304无线鼠标',  149.00,  1, NOW()),   -- 订单2
(3, 4, '优衣库 摇粒绒外套', 199.00,  1, NOW()),   -- 订单3
(3, 5, '安踏 跑步鞋',       299.00,  1, NOW());  -- 订单3 (注:为凑数演示已调整)

-- ---------------- 支付记录（订单1 已支付成功） ----------------
INSERT INTO payment(order_no, user_id, amount, pay_type, trade_no, buyer_id, status, paid_at, raw_notify, create_time) VALUES
('20261008000101', 2, 1897.00, 'ALIPAY', '2026100822001400000099999', '2088102179999999', 'SUCCESS', NOW(), '{"trade_status":"TRADE_SUCCESS"}', NOW());

-- ---------------- 知识库文档（2 个样例，仅落库不预置向量数据；用户可在后台重新上传） ----------------
INSERT INTO kb_document(file_name, file_path, file_url, file_type, file_size, chunk_count, status, uploader_id, create_time, update_time) VALUES
('商城退换货政策.txt', 'D:/uploads4/kb/退换货政策.txt', '/uploads/kb/退换货政策.txt', 'TXT', 856, 2, 'READY', 1, NOW(), NOW()),
('会员等级与权益.md', 'D:/uploads4/kb/会员等级与权益.md', '/uploads/kb/会员等级与权益.md', 'MD', 1240, 3, 'READY', 1, NOW(), NOW());

-- ---------------- 知识库分块（对应上面2个文档的占位分块，向量ID为示例） ----------------
INSERT INTO kb_chunk(document_id, chunk_no, vector_id, content, create_time) VALUES
(1, 1, 'kb-doc1-chunk1', '【退换货政策】商品签收后7天内，如商品存在质量问题，可申请无理由退货，运费由商城承担。', NOW()),
(1, 2, 'kb-doc1-chunk2', '【退换货政策】非质量问题退货需保证商品未拆封、配件齐全，运费由买家承担，退款将在收到退货后3个工作日内原路退回。', NOW()),
(2, 1, 'kb-doc2-chunk1', '【会员等级】累计消费满500元升级为银卡会员，满2000元升级为金卡会员，满5000元升级为钻石会员。', NOW()),
(2, 2, 'kb-doc2-chunk2', '【会员权益】金卡及以上会员享受全场商品9折、生日月双倍积分、专属客服优先接入等权益。', NOW()),
(2, 3, 'kb-doc3-chunk3', '【积分规则】每消费1元积1分，100积分可在积分商城兑换5元无门槛优惠券。', NOW());

-- ---------------- 客服聊天记录（zhangsan 与客服的一轮示例对话） ----------------
INSERT INTO chat_message(user_id, role, content, create_time) VALUES
(2, 'user',      '你好，请问下订单多久能发货？', NOW()),
(2, 'assistant', '您好！正常情况下，订单支付成功后24小时内由仓库发出，物流时效1-3天，偏远地区3-5天。', NOW());
