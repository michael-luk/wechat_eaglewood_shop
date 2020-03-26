package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.exception.WxErrorException;
import models.*;
import models.common.CompanyModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import play.Play;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.*;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static play.data.Form.form;

public class Application extends Controller implements IConst {
    public static String domainName = "benxiangyuan.com";
    // public static String domainName = "yf-china.com";
    public static String domainNameWithProtocal = "http://" + domainName;
    public static float thumbSize = 250F;

    public static Result checkAlive() {
        return ok("alive");
    }

    // public static Result getCities() {
    // CityUtil cityUtil = new CityUtil();
    // List provinces = cityUtil.getProvinces();
    // return play.mvc.Results.TODO;
    // }

    public static class LoginParser {

        public String username;
        public String password;

        public String validate() {
            if (password != null && password.length() < 32) {
                password = LyLib.Utils.MD5.getMD5(password);
            }
            if (UserModel.authenticate(username, password) == null) {
                return "用户名或密码不正确";
            }
            return null;
        }
    }

    public static class CartParser {

        public List<CartItemParser> items;

        // public String validate() {
        // if (password != null && password.length() < 32) {
        // password = LyLib.Utils.MD5.getMD5(password);
        // }
        // if (UserModel.authenticate(username, password) == null) {
        // return "用户名或密码不正确";
        // }
        // return null;
        // }
    }

    public static class CartItemParser {

        public Long pid;
        public Integer num;
        public ProductModel product;
        public Boolean select = true;

        // public String validate() {
        // if (password != null && password.length() < 32) {
        // password = LyLib.Utils.MD5.getMD5(password);
        // }
        // if (UserModel.authenticate(username, password) == null) {
        // return "用户名或密码不正确";
        // }
        // return null;
        // }
    }

    public static Result setCart() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());

        Msg<CartParser> msg = new Msg<>();
        Form<CartParser> httpForm = form(CartParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            CartParser formObj = httpForm.get();

            if (formObj.items == null) {
                formObj.items = new ArrayList<>();
            } else {
                // 防止session过长
                for (CartItemParser item : formObj.items) {
                    item.product = null;
                }
            }

            session("CART", Json.toJson(formObj).toString());
            msg.flag = true;
            msg.data = formObj;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: 更新购物车");
        } else {
            play.Logger.error(DateUtil.Date2Str(new Date()) + " - result: 更新购物车失败");
            msg.message = httpForm.errors().toString();
        }
        return ok(Json.toJson(msg));
    }

    public static Result getCart() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());

        Msg<String> msg = new Msg<>();
        String sessionCart = session("CART");
        if (StrUtil.isNull(sessionCart)) {
            sessionCart = "{\"items\":[]}";
            msg.data = sessionCart;
            session("CART", sessionCart);
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: 创建空购物车");
        } else {
            msg.flag = true;
            msg.data = sessionCart;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: 获取购物车 - " + sessionCart);
        }

        return ok(Json.toJson(msg));
    }

    public static Result clearCart() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());

        session("CART", "{\"items\":[]}");
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: 清除购物车session");

        return ok("清除购物车session");
    }

    public static Result clearSession() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());

        session().clear();
        session("WX_OPEN_ID", "");
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: 已清除所有session");
        return ok("已清除所有session");
    }

    public static Result login() {
        UserModel userModel = UserModel.findByloginName(session(SESSION_USER_NAME));
        if (userModel != null && userModel.userRole == 2) {
            // return redirect("assets/backend/index.html#/");
            return ok();
        } else
            return ok();
    }

    public static Result backendLogin() {
        UserModel userModel = UserModel.findByloginName(session(SESSION_USER_NAME));
        if (userModel != null && userModel.userRole == 2) {
            return redirect(routes.OrderController.backendPage());
        } else
            return ok(backend_login.render(form(LoginParser.class)));
    }

    public static Result logout() {
        session().clear();
        flash("logininfo", "您已登出,请重新登录");
        return redirect(routes.Application.login());
    }

    public static Result backendLogout() {
        session().clear();
        flash("logininfo", "您已登出,请重新登录");
        return redirect(routes.Application.backendLogin());
    }

    public static Result backendPage() {
        return redirect(routes.OrderController.backendPage());
    }

    public static Result errorPage() {
        return ok(errpage.render());
    }

    public static Result blankPage4Platform() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());
        return ok(blank4Weixin.render());
    }

    public static Result blankPage4WeixinOpenId() {
        return ok(blank4Weixin.render());
    }

    public static Result onePage(String resellerCode) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));
        play.Logger.info("resellerCode: " + resellerCode);

        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());


        if (session("WX_OPEN_ID") == null || StrUtil.isNotNull(resellerCode)) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode=" + resellerCode
                    + "%26path=home" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_one.render());
        }
//		session("userid", "2");
//		return ok(dm_one.render());
    }

    public static Result devOnePage(String resellerCode) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));
        play.Logger.info("resellerCode: " + resellerCode);

        session("userid", "2");
        return ok(dm_one.render());
    }

    public static Result homePage(String resellerCode) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=dmhome" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_home.render());
        }
    }

    // 砍价首页
    public static Result bargainPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());
        play.Logger.info("loading with session: " + session("WX_OPEN_ID"));

        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());


        if (StrUtil.isNull(session("WX_OPEN_ID"))) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode=" + "%26path=bargain"
                    + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(bargain.render());
        }
    }

    // 斩价邀请朋友
    public static Result bargain4FriendsPage(Long aid) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));
        play.Logger.info("砍价活动线id: " + aid.toString());

        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());

       /* return redirect("/bargain/go/go");*/
//        return ok(bargain4friends.render(aid.toString()));
//        return redirect("/bargain/go/" + aid.toString());

        if (aid == 0) {// 无活动ID则引导到介绍页
            return redirect(
                    "http://mp.weixin.qq.com/s?__biz=MzI0NTE4OTkxNA==&mid=402891824&idx=1&sn=653c604d8cec67c35f3580bb7b3b910e#rd");
        }

        if (StrUtil.isNull(session("WX_OPEN_ID"))) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode=" + aid.toString()
                    + "%26path=bargain4friend" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);

            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(bargain4friends.render(aid.toString()));
            //return redirect("/bargain/go/" + aid.toString());
        }
    }

    public static Result userCenterPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode=%26path=userCenter"
                    + "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_userCenter.render());
        }
    }

    public static Result orderPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));


        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

       if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=order" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_order.render());
       }

    }

    public static Result myorderPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束


        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=myorder" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_myorder.render());
        }


    }

    public static Result couponPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束
        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=coupon" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_coupon.render());
        }

    }

    public static Result addressPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束


        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=address" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_address.render());
        }

    }

    public static Result particularsPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));


        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());


        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=particulars" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_particulars.render());
        }
    }

    public static Result distributorPage() {

        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=distributor" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_distributor.render());
        }

    }

    public static Result enshrinePage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=enshrine" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_enshrine.render());
        }
    }

    public static Result distributorOrderPage() {


        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=distributorOrder" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_distributorOrder.render());
        }

    }

    public static Result teamPage() {

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束
        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=team" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_team.render());
        }
    }


    public static Result allProductPage() {

        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=allProduct" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_allProduct.render());
        }
    }

    public static Result myerweiPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=myerwei" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_myerwei.render());

        }


    }

    public static Result otherweiPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));


        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束
        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=otherwei" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_otherwei.render());
        }


    }

    public static Result PayPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));


        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=pay" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_pay.render());
        }

    }

    public static Result indentPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=indent" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_indent.render());
        }

    }

    public static Result catPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=cat" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_cat.render());
        }

    }

    public static Result aboutPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=about" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_about.render());
        }

    }

    public static Result contactPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=contact" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_contact.render());
        }

    }

    public static Result dispatchingPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }


//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=dispatching" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_dispatching.render());
        }

    }

    public static Result AftermarketPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));

        //微信分享
        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());
        //微信分享结束

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode="
                    + "%26path=Aftermarket" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(dm_Aftermarket.render());
        }
    }


    public static Result buyImmediatelyPage() {
        return ok(BuyImmediately.render());
    }

    public static Result bargainactivityendPage() {
        return ok(bargainActivityEnd.render());
    }

    /* pc端 */
    public static Result homePcPage() {
        return ok(pc_home.render());
    }

    public static Result productPcPage() {
        return ok(pc_product.render());
    }

    public static Result culturePcPage() {
        return ok(pc_culture.render());
    }
    public static Result jidiPage() {
        return ok(pc_jidi.render());
    }

    public static Result aboutPcPage() {
        return ok(pc_about.render());
    }

    public static Result newsPcPage() {
        return ok(pc_news.render());
    }

    public static Result news2PcPage() {
        return ok(pc_news2.render());
    }
    public static Result productmessagePcPage() {
        return ok(pc_productMessage.render());
    }
    public static Result promptPage() {
        return ok(prompt.render());
    }

    /*集团官网*/
    public static Result homeBlPage() {
        return ok(bloc_home.render());
    }
    public static Result joinBlPage() {
        return ok(bloc_join.render());
    }
    public static Result contactBlPage() {
        return ok(bloc_contact.render());
    }
    public static Result profileBlPage() {
        return ok(bloc_profile.render());
    }
    public static Result historyBlPage() {
        return ok(bloc_history.render());
    }
    public static Result honorBlPage() {
        return ok(bloc_honor.render());
    }
    public static Result planBlPage() {
        return ok(bloc_plan.render());
    }
    public static Result addBlPage() {
        return ok(bloc_add.render());
    }
    public static Result newsBlPage() {
        return ok(bloc_news.render());
    }
    public static Result news2BlPage() {
        return ok(bloc_news2.render());
    }
    public static Result travelBlPage() {
        return ok(bloc_travel.render());
    }

    public static Result staffBlPage() {
        return ok(bloc_staff.render());
    }
    public static Result staff2BlPage() {
        return ok(bloc_staff2.render());
    }
    public static Result matrixaBlPage() {
        return ok(bloc_matrixa.render());
    }
    public static Result matrixa2BlPage() {
        return ok(bloc_matrixa2.render());
    }
    public static Result matrixbBlPage() {
        return ok(bloc_matrixb.render());
    }
    public static Result matrixb2BlPage() {
        return ok(bloc_matrixb2.render());
    }
    public static Result chairmanBlPage() {
        return ok(bloc_chairman.render());
    }
    public static Result chairman2BlPage() {
        return ok(bloc_chairman2.render());
    }
    public static Result introBlPage() {
        return ok(bloc_intro.render());
    }
    public static Result intro2BlPage() {
        return ok(bloc_intro2.render());
    }
    public static Result mediaBlPage() {
        return ok(bloc_media.render());
    }
    public static Result media2BlPage() {
        return ok(bloc_media2.render());
    }

    /**
     * Handle login form submission.
     */
    // 登录验证
    public static Result authenticate() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());
        Form<LoginParser> loginForm = form(LoginParser.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            play.Logger.error(DateUtil.Date2Str(new Date()) + " - " + loginForm.errors().toString());
            return badRequest(login.render(loginForm));// @form.globalError.message
        } else {
            UserModel userModel = UserModel.findByloginName(loginForm.get().username);
            session().clear();
            session(SESSION_USER_NAME, userModel.loginName);
            session(SESSION_USER_ID, userModel.id.toString());
            if (userModel != null) {
                Integer role = userModel.userRole;
                session(SESSION_USER_ROLE, role.toString());
                if (role > 0) {
                    return ok(dm_home.render());
                } else {
                    // return redirect("assets/backend/index.html#/");
                    return forbidden("登录失败, 权限不足");
                }
            }
            return redirect(routes.Application.login());
        }
    }

    // 登录验证
    public static Result backendAuthenticate() {
        Form<LoginParser> loginForm = form(LoginParser.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            play.Logger.info(DateUtil.Date2Str(new Date()) + " form error: " + loginForm.errors().toString());
            // return badRequest(login.render(loginForm));//
            flash("logininfo", "登录失败,请重试");
            return redirect(routes.Application.backendLogin());
        } else {
            UserModel userModel = UserModel.findByloginName(loginForm.get().username);
            session().clear();
            session(SESSION_USER_NAME, userModel.loginName);
            session(SESSION_USER_ID, userModel.id.toString());
            if (userModel != null) {
                Integer role = userModel.userRole;
                session(SESSION_USER_ROLE, role.toString());
                if (role > 0) {// 1管理员, 2超级管理员
                    // TODO: 检查当前登录和最后一次登录IP, 如果不同, 要报警通知
                    // 更新最后一次登录的IP
                    userModel.lastLoginIP = request().remoteAddress();
                    Ebean.update(userModel);
                    return redirect(routes.OrderController.backendPage());
                } else {
                    // return redirect("assets/backend/index.html#/");
                    return forbidden("您没有权限登录后台");
                }
            }
            return redirect(routes.Application.backendLogin());
        }
    }

    // @Security.Authenticated(Secured.class)
    // @Cached(key = "showImage")
    public static Result showImage(String filename) {
        String path = Play.application().path().getPath() + "/public/upload/" + filename;

        try {
            response().setContentType("image");
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    IOUtils.toByteArray(new FileInputStream(new File(path))));
            return ok(bais);
        } catch (IOException e) {
            // e.printStackTrace();
        }
        return notFound(filename + " is Not Found!");
    }

    // @Security.Authenticated(Secured.class)
    public static Result showBarcode(String filename) {
        String path = Play.application().path().getPath() + "/public/barcode/" + filename;

        try {
            response().setContentType("image");
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    IOUtils.toByteArray(new FileInputStream(new File(path))));
            return ok(bais);
        } catch (IOException e) {
            // e.printStackTrace();
        }
        return notFound(filename + " barcode is Not Found!");
    }

    // @Security.Authenticated(Secured.class)
    // @Cached(key = "showImg")
    public static Result showImg(String folder, String filename) {
        String path = Play.application().path().getPath() + "/public/" + folder + "/" + filename;

        try {
            response().setContentType("image");
            return ok(getImageByte(path));
        } catch (IOException ex) {
            play.Logger.error(DateUtil.Date2Str(new Date()) + " - 找不到图片: " + folder + filename);
        }
        return notFound(folder + filename + " is Not Found!");
    }

    public static ByteArrayInputStream getImageByte(String path) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.toByteArray(new FileInputStream(new File(path))));
        return bais;
    }

    // @Security.Authenticated(Secured.class)
    public static Result uploadImage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());

        Msg<String> msg = new Msg<>();

        Http.MultipartFormData body = request().body().asMultipartFormData();

        Map map = body.asFormUrlEncoded();
        if (!map.containsKey("className") || !map.containsKey("property")) {
            msg.message = PARAM_ISSUE;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
            return ok(Json.toJson(msg));
        }
        Long cid = 0l;
        if (map.containsKey("cid")) {
            cid = Long.parseLong(form().bindFromRequest().data().get("cid"));
        }
        String className = form().bindFromRequest().data().get("className");
        String property = form().bindFromRequest().data().get("property");

        Http.MultipartFormData.FilePart imgFile = body.getFile("file");
        if (imgFile != null) {
            // 图片地址及文件名, 以毫秒命名的文件名如"1449837445671"
            String path = Play.application().path().getPath() + "/public/upload/";
            String destFileName = String.valueOf(System.currentTimeMillis());

            String contentType = imgFile.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                msg.message = "error:not image file";
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                return ok(Json.toJson(msg));
            }

            File file = imgFile.getFile();
            try {
                // 生成原始图片
                FileUtils.copyFile(file, new File(path + destFileName));
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - upload img success");
                // 生成缩略图
                String thumbNailPath = Play.application().path().getPath() + "/public/thumb/";
                try {
                    if (!GenerateThumbNailImg(path + destFileName, thumbNailPath + destFileName, thumbSize))
                        play.Logger.info(DateUtil.Date2Str(new Date()) + " - generate thumbnail img issue: unknown");
                    else
                        play.Logger.info(DateUtil.Date2Str(new Date()) + " - generate thumbnail img success");

                } catch (Exception ex) {
                    play.Logger.error(
                            DateUtil.Date2Str(new Date()) + " - generate thumbnail img issue: " + ex.getMessage());
                }

                // 不指定ID也可以上传图片, 直接返回文件名
                if (cid == 0l) {
                    msg.flag = true;
                    msg.data = destFileName;
                    play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + destFileName);
                    return ok(Json.toJson(msg));
                }

                // 更新模型的字段为文件名
                if ("CompanyModel".equals(className)) {
                    CompanyModel found = CompanyModel.find.byId(cid);
                    if (found != null) {
                        if ("logo1".equals(property)) {
                            found.logo1 = destFileName;
                            Ebean.update(found);
                        } else if ("barcodeImg1".equals(property)) {
                            found.barcodeImg1 = destFileName;
                            Ebean.update(found);
                        } else if ("barcodeImg2".equals(property)) {
                            found.barcodeImg2 = destFileName;
                            Ebean.update(found);
                        } else if ("barcodeImg3".equals(property)) {
                            found.barcodeImg3 = destFileName;
                            Ebean.update(found);
                        } else {
                            msg.message = PARAM_ISSUE;
                            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                            return ok(Json.toJson(msg));
                        }
                    } else {
                        msg.message = NO_FOUND;
                        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                        return ok(Json.toJson(msg));
                    }
                } else if ("CatalogModel".equals(className)) {
                    CatalogModel found = CatalogModel.find.byId(cid);
                    if (found != null) {
                        if ("images".equals(property)) {
                            found.images = destFileName;
                            Ebean.update(found);
                        } else if ("smallImages".equals(property)) {
                            found.smallImages = destFileName;
                            Ebean.update(found);
                        } else {
                            msg.message = PARAM_ISSUE;
                            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                            return ok(Json.toJson(msg));
                        }
                    } else {
                        msg.message = NO_FOUND;
                        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                        return ok(Json.toJson(msg));
                    }
                } else if ("InfoModel".equals(className)) {
                    InfoModel found = InfoModel.find.byId(cid);
                    if (found != null) {
                        if ("images".equals(property)) {
                            if (LyLib.Utils.StrUtil.isNull(found.images)) {
                                found.images = destFileName;
                            } else {
                                found.images += "," + destFileName;
                            }
                            Ebean.update(found);
                        } else if ("smallImages".equals(property)) {
                            if (LyLib.Utils.StrUtil.isNull(found.smallImages)) {
                                found.smallImages = destFileName;
                            } else {
                                found.smallImages += "," + destFileName;
                            }
                            Ebean.update(found);
                        } else {
                            msg.message = PARAM_ISSUE;
                            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                            return ok(Json.toJson(msg));
                        }
                    } else {
                        msg.message = NO_FOUND;
                        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                        return ok(Json.toJson(msg));
                    }
                } else if ("ProductModel".equals(className)) {
                    ProductModel found = ProductModel.find.byId(cid);
                    if (found != null) {
                        if ("images".equals(property)) {
                            if (LyLib.Utils.StrUtil.isNull(found.images)) {
                                found.images = destFileName;
                            } else {
                                found.images += "," + destFileName;
                            }
                            Ebean.update(found);
                        } else if ("descriptionImages".equals(property)) {
                            if (LyLib.Utils.StrUtil.isNull(found.descriptionImages)) {
                                found.descriptionImages = destFileName;
                            } else {
                                found.descriptionImages += "," + destFileName;
                            }
                            Ebean.update(found);
                        } else {

                            msg.message = PARAM_ISSUE;
                            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                            return ok(Json.toJson(msg));
                        }
                    } else {
                        msg.message = NO_FOUND;
                        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                        return ok(Json.toJson(msg));
                    }
                } else if ("ThemeModel".equals(className)) {
                    ThemeModel found = ThemeModel.find.byId(cid);
                    if (found != null) {
                        if ("images".equals(property)) {
                            if (LyLib.Utils.StrUtil.isNull(found.images)) {
                                found.images = destFileName;
                            } else {
                                found.images += "," + destFileName;
                            }
                            Ebean.update(found);
                        } else {
                            msg.message = PARAM_ISSUE;
                            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                            return ok(Json.toJson(msg));
                        }
                    } else {
                        msg.message = NO_FOUND;
                        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                        return ok(Json.toJson(msg));
                    }
                } else if ("FoodcommentModel".equals(className)) {
                    FoodcommentModel found = FoodcommentModel.find.byId(cid);
                    if (found != null) {
                        if ("images".equals(property)) {
                            if (LyLib.Utils.StrUtil.isNull(found.images)) {
                                found.images = destFileName;
                            } else {
                                found.images += "," + destFileName;
                            }
                            Ebean.update(found);
                        } else {
                            msg.message = PARAM_ISSUE;
                            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                            return ok(Json.toJson(msg));
                        }
                    } else {
                        msg.message = NO_FOUND;
                        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                        return ok(Json.toJson(msg));
                    }
                }
                msg.flag = true;
                msg.data = destFileName;
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + destFileName);
                return ok(Json.toJson(msg));
            } catch (IOException e) {
                msg.message = e.getMessage();
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                return ok(Json.toJson(msg));
            }
        }
        msg.message = "error:Missing file";
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
        return ok(Json.toJson(msg));
    }

    public static boolean GenerateThumbNailImg(String baseFilePath, String thumbNailPath, float tagsize)
            throws Exception {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());

        if (tagsize == 0)
            tagsize = 100;

        String newUrl = thumbNailPath;
        java.awt.Image bigJpg = javax.imageio.ImageIO.read(new java.io.File(baseFilePath));

        if (bigJpg == null) {
            return false;
        }

        int old_w = bigJpg.getWidth(null);
        int old_h = bigJpg.getHeight(null);
        int new_w = 0;
        int new_h = 0;

        float tempdouble;
        tempdouble = old_w > old_h ? old_w / tagsize : old_h / tagsize;
        new_w = Math.round(old_w / tempdouble);
        new_h = Math.round(old_h / tempdouble);

        java.awt.image.BufferedImage tag = new java.awt.image.BufferedImage(new_w, new_h,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        tag.getGraphics().drawImage(bigJpg, 0, 0, new_w, new_h, null);

        try {
            File outputfile = new File(newUrl);
            ImageIO.write(tag, "png", outputfile);
        } catch (IOException e) {

        }
        return true;
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result generateAllThumbNailImg(float tagsize) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());

        if (tagsize == 0)
            tagsize = thumbSize;

        String path = Play.application().path().getPath() + "/public/upload/";
        File file = new File(path);
        String[] fileNameList = file.list();

        String thumbNailPath = Play.application().path().getPath() + "/public/thumb/";
        for (String fileName : fileNameList) {
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - generate thumb nail img: " + fileName);
            // 生成缩略图
            try {
                if (!GenerateThumbNailImg(path + fileName, thumbNailPath + fileName, tagsize))
                    play.Logger.info(DateUtil.Date2Str(new Date()) + " - generate thumbnail img issue: unknown");
                else
                    play.Logger.info(DateUtil.Date2Str(new Date()) + " - generate thumbnail img success");
            } catch (Exception ex) {
                play.Logger
                        .error(DateUtil.Date2Str(new Date()) + " - generate thumbnail img issue: " + ex.getMessage());
                return notFound("生成图片的缩略图出错");
            }
        }
        return ok("已生成所有图片的缩略图");
    }

    public static Map<String, String> getMapFromRequest(Map<String, String[]> requestMap){
        Map<String, String> resultMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : requestMap.entrySet()){
            String entryValueStr = "";
            if (entry.getValue() != null){
                for (String entryValue : entry.getValue()){
                    entryValueStr += entryValue;
                }
            }
            resultMap.put(entry.getKey(), entryValueStr);
        }
        return resultMap;
    }
}
