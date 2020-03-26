package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.StrUtil;
import com.alipay.config.AlipayConfig;
import com.alipay.sign.RSA;
import com.alipay.util.AlipayCore;
import com.alipay.util.AlipaySubmit;
import com.avaje.ebean.Ebean;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpCustomMessage;
import models.EnjoyTheCodeModel;
import models.OrderModel;
import models.UserModel;
import net.sf.uadetector.ReadableDeviceCategory;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.w3c.dom.Document;
import play.api.mvc.RequestTaggingHandler;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.XPath;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.alipay;
import views.html.alipayHint;

import java.util.*;

import static play.data.Form.form;

public class AlipayController extends Controller implements IConst {

    // yifeng
    public static String alipayEmailUserName = "a31196@qq.com";
    public static String merchantId = "2088022666605043";
    public static String seckey = "k0689b8jkbkud6xaaw4vne4mhwu6d0ow";
    public static String appId = "2016011201087266";
    public static String aliPayPublicKey =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";

    public static String returnUrl = Application.domainNameWithProtocal + "/w/prompt";
    public static String notifyUrl = Application.domainNameWithProtocal + "/alipay/pay/notify";
    public static String signType = "MD5";
    //    public static String signType = "RSA";
    public static String lang = "utf-8";

    //    @Security.Authenticated(Secured.class)
    public static Result preparePay(Long oid) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());

        String userAgentStr = request().getHeader("User-Agent");

        UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
        ReadableUserAgent agent = parser.parse(userAgentStr);
        ReadableDeviceCategory device = agent.getDeviceCategory();

        // 当检测到weixin, 进入提示页, 否则继续流程
        if ("personal computer".equals(device.getCategory().getName().toLowerCase())) {
            // if pc
            play.Logger.info("Alipay from PC");
        } else {
            //if 微信浏览器
            if (request().getHeader("User-Agent").indexOf("MicroMessenger") > -1) {
                play.Logger.info("Alipay from Weixin");
                return ok(alipayHint.render());
            } else {
                // if 非微信手机浏览器
                play.Logger.info("Alipay from mobile browser");
            }
        }

        if (oid == 0) {
            play.Logger.error("微信支付结果: 订单ID不正确, 请重新下单");
            flash("error", "微信支付结果: 订单ID不正确, 请重新下单");
            return redirect(routes.Application.errorPage());
        }
//        if (StrUtil.isNull(session(SESSION_USER_ID))) {
//            play.Logger.error("微信支付结果: 用户未登录, 请重新进入商城并下单");
//            flash("error", "微信支付结果: 用户未登录, 请重新进入商城并下单");
//            return redirect(routes.Application.errorPage());
//        }

        OrderModel order = OrderModel.find.byId(oid);
        if (order == null || order.orderProducts == null || order.orderProducts.size() <= 0
                || StrUtil.isNull(order.orderNo) || order.amount <= 0) {
            play.Logger.error("微信支付结果: 订单数据不完整, 请重新进入商城并下单");
            flash("error", "微信支付结果: 订单数据不完整, 请重新进入商城并下单");
            return redirect(routes.Application.errorPage());
        }

        play.Logger.info("Start alipay proc");
        Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("service", "create_direct_pay_by_user");
        sParaTemp.put("partner", merchantId);
        sParaTemp.put("_input_charset", lang);
        sParaTemp.put("payment_type", "1");
        sParaTemp.put("notify_url", notifyUrl);
        sParaTemp.put("return_url", returnUrl);
        sParaTemp.put("seller_email", alipayEmailUserName);
        sParaTemp.put("out_trade_no", order.orderNo);//我们系统的订单号
        sParaTemp.put("total_fee", "0.01"); // order.amount

        String subject = order.orderNo.toUpperCase();
//        String subject = order.orderProducts.get(0).name;
//        if (order.orderProducts.size() > 1) {
//            subject += " 等" + order.orderProducts.size() + "种商品";
//        }
        sParaTemp.put("subject", subject);

        Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);//去掉空值
        String sParaStr = AlipayCore.createLinkString(sPara);//把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        play.Logger.info("Alipay para string: " + sParaStr);

        String sign = "";//得到签名
//        if (signType.equals("RSA")) {
//            play.Logger.info("Do alipay RSA signature.");
//            sign = RSA.sign(sParaStr, seckey, lang);
//        }
        if ("MD5".equals(signType)) {
            play.Logger.info("Do alipay MD5 signature.");
            String sign4md5 = sParaStr + seckey;
            sign = LyLib.Utils.MD5.getMD5(sign4md5);
        }
        play.Logger.info("Alipay signature: " + sign);

        if (StrUtil.isNull(sign)) {
            play.Logger.error("支付宝签名错误.");
            flash("error", "支付宝签名错误.");
            return redirect(routes.Application.errorPage());
        }

        flash("service", "create_direct_pay_by_user");
        flash("partner", merchantId);
        flash("_input_charset", lang);
        flash("payment_type", "1");
        flash("notify_url", notifyUrl);
        flash("return_url", returnUrl);
        flash("seller_email", alipayEmailUserName);
        flash("out_trade_no", order.orderNo);//我们系统的订单号
        flash("total_fee", "0.01"); // order.amount
        flash("subject", subject);
        flash("sign", sign);
        flash("sign_type", signType);

        flash("orderNo", order.orderNo.toUpperCase());
        flash("orderAmount", String.valueOf(order.amount));
        return redirect("/alipay/pay/go");
    }

    public static Result doPay() {
        return ok(alipay.render());
    }

    public static Result getPayNotify() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().toString());

        // 转换为map
        Map<String, String> sParaTemp = Application.getMapFromRequest(request().body().asFormUrlEncoded());
        play.Logger.info("Alipay notify map data size: " + sParaTemp.size());

        String alipayNotifySign = sParaTemp.get("sign");
        String alipayNotifySignType = sParaTemp.get("sign_type");
        play.Logger.info("Alipay notify signature: " + alipayNotifySign);

        if (!"MD5".equals(alipayNotifySignType)) {
            //TODO 错误处理
            play.Logger.error("Alipay notify SignType NOT match");
        }

        Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);//去掉空值
        String sParaStr = AlipayCore.createLinkString(sPara);//把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        play.Logger.info("Alipay notify para string: " + sParaStr);

        String sign4md5 = sParaStr + seckey;
        String sign = LyLib.Utils.MD5.getMD5(sign4md5);
        play.Logger.info("Alipay notify calculated signature: " + sign);

        // 验证签名
        if (sign.equals(alipayNotifySign)) {
            // TODO: 处理订单
            play.Logger.info("Alipay notify signature match");

 /*            if (sParaStr == null) {
                play.Logger.error("null xml notify from wx pay");
                return ok(
                        "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[xml is null]]></return_msg></xml>");
            }

           String return_code = XPath.selectText("//return_code", sParaStr);
            String return_msg = XPath.selectText("//return_msg", sParaStr);
            String result_code = XPath.selectText("//result_code", sParaStr);
            String err_code = XPath.selectText("//err_code", sParaStr);
            String err_code_des = XPath.selectText("//err_code_des", sParaStr);
            String notify_id = XPath.selectText("//notify_id", sParaStr);
            String total_fee = XPath.selectText("//total_fee", sParaStr);
            String buyer_id = XPath.selectText("//buyer_id", sParaStr);
            String out_trade_no = XPath.selectText("//out_trade_no", sParaStr);
            String notify_time = XPath.selectText("//notify_time", sParaStr);
            String bank_type = XPath.selectText("//bank_type", sParaStr);*/

           String trade_status = sParaTemp.get("trade_status");
            String return_msg = sParaTemp.get("trade_status");
            String result_code = sParaTemp.get("trade_status");
        /*    String err_code = sParaTemp.get("err_code");
            String err_code_des = sParaTemp.get("err_code_des");*/
            String notify_id = sParaTemp.get("notify_id");
            String total_fee = sParaTemp.get("total_fee");
            String out_trade_no = sParaTemp.get("out_trade_no");
            String notify_time = sParaTemp.get("notify_time");
            String seller_email = sParaTemp.get("seller_email");

            play.Logger.info(trade_status);
        /*    play.Logger.info(err_code);
            play.Logger.info(err_code_des);*/
            play.Logger.info(notify_id);
            play.Logger.info(total_fee);
            play.Logger.info(out_trade_no);
            play.Logger.info(notify_time);
            play.Logger.info(seller_email);


            OrderModel order = OrderModel.find.where().eq("orderNo", out_trade_no).findUnique();

            if (order == null) {
                play.Logger.error("** wx pay issue! order not found: " + out_trade_no);
                return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
                        + " not found]]></return_msg></xml>");
            }

            // 订单若是“已支付”， 这回复WXPAY, 免得一直通知
//            if (order.status == 1) {
//                return ok(
//                        "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
//            }

            // 订单应该是"0新建"或"12等待支付结果"的
            if (order.status != 0 && order.status != 12) {
                order.comment = "***收到支付平台通知,但是订单状态不是[新建], id: " + order.id.toString() + " status: " + order.status;
                Ebean.update(order);
                play.Logger.error("get pay notify from ali: " + order.comment);
                return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
                        + " status not correct, should be NOTPAY]]></return_msg></xml>");
            }

            order.payReturnCode = trade_status;
            order.payReturnMsg = trade_status;
            order.payResultCode = trade_status;
            order.payTransitionId = notify_id;
            order.payAmount = total_fee;
            order.payBank = seller_email;
            order.payRefOrderNo = out_trade_no;
            order.paySign = sign;
            order.payTime = notify_time;
            order.payThirdPartyId = notify_id;


            // 处理失败订单
            if (!"TRADE_SUCCESS".equals(trade_status)) {
                play.Logger.error("** ali pay issue! return_msg=" + return_msg /*+ ", err_code=" + err_code + ", err_code_des="
                        + err_code_des*/);

                order.status = 9;// 设置为支付失败
                Ebean.update(order);
                play.Logger.error("** ali pay issue! update order to fail: " + order.id);
                return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
                        + ", trade_status:" + trade_status + ", return_msg=" + return_msg /*+ ", err_code=" + err_code
                        + ", err_code_des=" + err_code_des */+ "]]></return_msg></xml>");
            }

            if (seller_email.equals(alipayEmailUserName)) {
                play.Logger.info("seller_email:" + seller_email);
            }else{
                play.Logger.error("** ali pay issue! 支付宝账户不匹配: seller_email: " + seller_email);

                order.status = 11;// 设置为数据有误
                Ebean.update(order);
                play.Logger.error("** ali pay issue! 支付宝账户不匹配! update order to fail: " + order.id);
                return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
                        + ", trade_status:" + trade_status + ", return_msg=" + return_msg );
            }

            // 校验购买用户, 以及处理用户积分
            UserModel buyer = UserModel.find.byId(order.refBuyerId);
           /* if (buyer == null || StrUtil.isNull(notify_id) || !notify_id.equals(buyer.wxOpenId)) {
                play.Logger.error("** ali pay issue! 购买用户不匹配: buyer.openid: " + buyer.wxOpenId);

                order.status = 11;// 设置为数据有误
                Ebean.update(order);
                play.Logger.error("** ali pay issue! 购买用户不匹配! update order to fail: " + order.id);
                return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
                        + ", trade_status:" + trade_status + ", return_msg=" + return_msg *//*+ ", err_code=" + err_code
                        + ", err_code_des=" + err_code_des + "]]></return_msg></xml>"*//*);
            } else {*/
                Double productAmount = new Double(order.productAmount);
                play.Logger.info(buyer.nickname + " 用户下单得到积分 + " + productAmount.toString());
                buyer.handleJifen(productAmount.intValue(),order.jifen);
                Ebean.update(buyer);
           /* }*/

            // 扣产品库存, 加卖出数
            if (order.orderProducts == null || StrUtil.isNull(order.quantity)) {
                play.Logger.error("** ali pay issue! 购买商品数据有误! order: " + order.id);

                order.status = 11;// 设置为数据有误
                Ebean.update(order);
                play.Logger.error("** ali pay issue! 购买商品数据有误! update order to fail: " + order.id);
                return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
                        + ", trade_status:" + trade_status + ", return_msg=" + return_msg /*+ ", err_code=" + err_code
                        + ", err_code_des=" + err_code_des*/ + "]]></return_msg></xml>");
            }

            List<Integer> quantityList = StrUtil.getIntegerListFromSplitStr(order.quantity);
            if (quantityList.size() <= 0 || quantityList.size() != order.orderProducts.size()) {
                play.Logger.error("** ali pay issue! 购买商品数量不匹配! order: " + order.id);

                order.status = 11;// 设置为数据有误
                Ebean.update(order);
                play.Logger.error("** ali pay issue! 购买商品数量不匹配! update order to fail: " + order.id);
                return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
                        + ", trade_status:" + trade_status + ", return_msg=" + return_msg /*+ ", err_code=" + err_code
                        + ", err_code_des=" + err_code_des */+ "]]></return_msg></xml>");
            }

            for (int i = 0; i < order.orderProducts.size(); i++) {
                order.orderProducts.get(i).inventory -= quantityList.get(i);
                play.Logger.info(order.orderProducts.get(i).name + " 库存 - " + quantityList.get(i).toString());
                order.orderProducts.get(i).soldNumber += quantityList.get(i);
                play.Logger.info(order.orderProducts.get(i).name + " 卖出 + " + quantityList.get(i).toString());
                Ebean.update(order.orderProducts.get(i));
            }
            // 最后处理订单
            order.status = 1;// 设为“已支付”
            Ebean.update(order);
            if (order.status == 1) {
                play.Logger.info("收到ali支付成功通知! update order: " + order.id);

                try {
                    // 给用户通知
                    String wxMessageText = "亲，您的订单已经支付成功！";
                    WxMpCustomMessage wxMessage = WxMpCustomMessage.TEXT().toUser(buyer.wxOpenId).content(wxMessageText).build();
                    WeiXinController.wxService.customMessageSend(wxMessage);

                    // 给管理员推送下单信息
                    for (String adminOpenId : WeiXinController.orderNotify2Admin.split(",")) {
                        WeiXinController.sendOrderMsgToWxUser(adminOpenId, order);
                    }

                    // 结束
                    play.Logger.info("通知：用户订单支付成功！" + order.orderNo);

                } catch (WxErrorException e) {
                    play.Logger.error("通知：用户订单支付错误" + order.orderNo);

                }
            }



        } else {
            // TODO: 错误处理
            play.Logger.error("Alipay notify signature NOT match");
        }

        return ok("got_it");
    }

    public static Result payReturn() {
        return play.mvc.Results.TODO;
    }
}
