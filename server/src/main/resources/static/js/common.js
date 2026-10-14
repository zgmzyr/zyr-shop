/**
 * zyr_shop 前台公共 JS 工具
 * 提供：登录态管理、fetch 封装、轻量提示、价格/日期格式化、状态文案
 * @author zyr
 */

/** cookie 中的 JWT 令牌 key 名 */
const TOKEN_KEY = 'zyr_token';

/** 用户昵称 cookie key */
const NICKNAME_KEY = 'zyr_nickname';

/**
 * 设置 cookie
 * @param {string} name 名称
 * @param {string} value 值
 * @param {number} days 有效天数
 */
function setCookie(name, value, days) {
    const expires = new Date(Date.now() + days * 24 * 60 * 60 * 1000).toUTCString();
    document.cookie = `${name}=${encodeURIComponent(value)};expires=${expires};path=/`;
}

/** 读取 cookie */
function getCookie(name) {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]*)(;|$)'));
    return match ? decodeURIComponent(match[2]) : '';
}

/** 删除 cookie */
function removeCookie(name) {
    document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`;
}

/** 获取当前 JWT 令牌 */
function getToken() {
    return getCookie(TOKEN_KEY);
}

/** 是否已登录 */
function isLoggedIn() {
    return !!getToken();
}

/**
 * 校验登录态，未登录则跳转登录页
 * @returns {boolean} true 表示已登录
 */
function requireLogin() {
    if (!isLoggedIn()) {
        location.href = '/login';
        return false;
    }
    return true;
}

/** 退出登录 */
function logout() {
    removeCookie(TOKEN_KEY);
    removeCookie(NICKNAME_KEY);
    location.href = '/login';
}

/**
 * 封装 fetch：自动带 Authorization 头、处理 JSON、401 跳登录
 * @param {string} url 接口地址
 * @param {object} options fetch 配置
 * @returns {Promise<any>} 解析后的 JSON 数据
 */
async function api(url, options = {}) {
    const token = getToken();
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    const resp = await fetch(url, { ...options, headers });
    let data;
    try {
        data = await resp.json();
    } catch (e) {
        // 非 JSON 响应（如支付 form HTML）
        return await resp.text();
    }
    // 业务约定：code = 200 成功
    if (data && typeof data.code !== 'undefined') {
        if (data.code === 200) {
            return data.data;
        }
        // 401 未登录
        if (data.code === 401) {
            removeCookie(TOKEN_KEY);
            location.href = '/login';
            throw new Error(data.message || data.msg || '未登录');
        }
        // 其它业务异常（后端 Result 统一返回 message 字段，兼容 msg 写法）
        throw new Error(data.message || data.msg || '请求失败');
    }
    return data;
}

/**
 * 轻量级 toast 提示
 * @param {string} msg 文本
 * @param {string} type 类型：默认/info/error/success
 */
function toast(msg, type = 'info') {
    const el = document.createElement('div');
    el.className = 'toast' + (type === 'error' ? ' error' : type === 'success' ? ' success' : '');
    el.textContent = msg;
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 2500);
}

/**
 * 格式化价格（保留 2 位小数）
 * @param {number} n 金额
 */
function formatPrice(n) {
    return '¥' + (Number(n) || 0).toFixed(2);
}

/**
 * 格式化日期时间（YYYY-MM-DD HH:mm）
 * @param {string|number} input 时间
 */
function formatTime(input) {
    if (!input) return '';
    const d = new Date(input);
    if (isNaN(d.getTime())) return input;
    const pad = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/**
 * 订单状态 → 中文文案
 * @param {string} status 状态枚举
 */
function orderStatusText(status) {
    const map = {
        PENDING: '待支付',
        PAID: '已支付',
        SHIPPED: '已发货',
        COMPLETED: '已完成',
        CANCELLED: '已取消'
    };
    return map[status] || status;
}

/** 订单状态 → CSS 标签类名 */
function orderStatusClass(status) {
    const map = {
        PENDING: 'tag-pending',
        PAID: 'tag-paid',
        SHIPPED: 'tag-shipped',
        COMPLETED: 'tag-completed',
        CANCELLED: 'tag-cancelled'
    };
    return map[status] || 'tag-completed';
}

/** 取 URL 查询参数 */
function getQueryParam(name) {
    return new URLSearchParams(location.search).get(name);
}

/** HTML 转义，防止注入 */
function escapeHtml(s) {
    return String(s || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
