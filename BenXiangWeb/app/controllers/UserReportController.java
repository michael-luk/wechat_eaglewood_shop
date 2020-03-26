
package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import models.*;
import models.common.CompanyModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.Region;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static play.data.Form.form;

public class UserReportController extends Controller implements IConst {

    public static Result exportOrderReport(long Id) {
        String fileName = DateUtil.NowString("yyyy_MM_dd_HH_mm_ss");//

        // 创建工作薄对象
        HSSFWorkbook workbook2007 = new HSSFWorkbook();
        // 把订单表放进集合
     /*   List<OrderModel> order;*/
            fileName += ".xls";
            //order = OrderModel.find.where().eq("status", 1).orderBy("id desc").findList();
           /* order = OrderModel.find.where().and(Expr.ne("status", 0),Expr.ne("status", 2)).and(Expr.ne("status", 3),Expr.ne("status", 9)).orderBy("id desc").findList();*/
        OrderModel order = OrderModel.find.byId(Id);
        // 创建单元格样式
        HSSFCellStyle cellStyle = workbook2007.createCellStyle();
        // 设置边框属性
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        // 指定单元格居中对齐
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 指定单元格垂直居中对齐
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 指定当单元格内容显示不下时自动换行
        cellStyle.setWrapText(true);
        // // 设置单元格字体
        HSSFFont font = workbook2007.createFont();
        font.setFontName("宋体");
        // 大小
        font.setFontHeightInPoints((short) 10);
        // 加粗
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        cellStyle.setFont(font);

        HSSFCellStyle style = workbook2007.createCellStyle();
        // 指定单元格居中对齐
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 指定单元格垂直居中对齐
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        HSSFFont font1 = workbook2007.createFont();
        font1.setFontName("宋体");
        font1.setFontHeightInPoints((short) 10);
        // 加粗
        font1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font1);

        // for (OrderModel module : order) {
        // List<RegInfo> regInfoList = RegInfo.find.where().eq("module_id",
        // module.id).findList();

        // List<OrderModel> order2 = new ArrayList<OrderModel>();
        // for (RegInfo regInfo : regInfoList) {
        // users.add(regInfo.user);
        // }

        // 创建工作表对象，并命名
        HSSFSheet sheet2 = workbook2007.createSheet("订单列表");
        // 设置列宽
        sheet2.setColumnWidth(0, 2200);
        sheet2.setColumnWidth(1, 2500);
        sheet2.setColumnWidth(2, 2500);
        sheet2.setColumnWidth(3, 3800);
        sheet2.setColumnWidth(5, 2500);
        sheet2.setColumnWidth(6, 2000);
        sheet2.setColumnWidth(7, 1800);
        sheet2.setColumnWidth(8, 1800);
        sheet2.setColumnWidth(9, 1800);
        sheet2.setColumnWidth(10, 2500);
        sheet2.setColumnWidth(11, 3000);
        sheet2.setColumnWidth(12, 3500);
        sheet2.setColumnWidth(13, 2500);
        sheet2.setColumnWidth(14, 2500);
        sheet2.setColumnWidth(15, 5000);

        sheet2.setDefaultColumnStyle(0, cellStyle);
        sheet2.setDefaultColumnStyle(1, cellStyle);
        sheet2.setDefaultColumnStyle(2, cellStyle);
        sheet2.setDefaultColumnStyle(3, cellStyle);
        sheet2.setDefaultColumnStyle(4, cellStyle);
        sheet2.setDefaultColumnStyle(5, cellStyle);
        sheet2.setDefaultColumnStyle(6, cellStyle);
        sheet2.setDefaultColumnStyle(7, cellStyle);
        sheet2.setDefaultColumnStyle(8, cellStyle);
        sheet2.setDefaultColumnStyle(9, cellStyle);
        sheet2.setDefaultColumnStyle(10, cellStyle);
        sheet2.setDefaultColumnStyle(11, cellStyle);
        sheet2.setDefaultColumnStyle(12, cellStyle);
        sheet2.setDefaultColumnStyle(13, cellStyle);
        sheet2.setDefaultColumnStyle(14, cellStyle);
        sheet2.setDefaultColumnStyle(15, cellStyle);


        // 创建表头
        HSSFRow title = sheet2.createRow(0);
        title.setHeightInPoints(50);
        title.createCell(0).setCellValue("本香");
        title.createCell(1).setCellValue("");
        title.createCell(2).setCellValue("");
        title.createCell(3).setCellValue("");
        title.createCell(4).setCellValue("");
        title.createCell(5).setCellValue("");
        title.createCell(6).setCellValue("");
        title.createCell(7).setCellValue("");
        title.createCell(8).setCellValue("");
        title.createCell(9).setCellValue("");
        title.createCell(10).setCellValue("");
        title.createCell(11).setCellValue("");
        title.createCell(12).setCellValue("");
        title.createCell(13).setCellValue("");
        title.createCell(14).setCellValue("");
        title.createCell(15).setCellValue("");
        sheet2.addMergedRegion(new Region(0, (short) 0, 0, (short) 15));
        HSSFCell ce = title.createCell((short) 1);

        HSSFRow titleRow = sheet2.createRow(1);
        // titleRow.setRowStyle(cellStyle);
        // 设置行高
        titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue("订单号");
        titleRow.createCell(1).setCellValue("下单时间");
        titleRow.createCell(2).setCellValue("购买用户");
        titleRow.createCell(3).setCellValue("产品名");
        titleRow.createCell(4).setCellValue("数量");
        titleRow.createCell(5).setCellValue("单价");
        titleRow.createCell(6).setCellValue("配送费 ");
        titleRow.createCell(7).setCellValue("商品总额");
        titleRow.createCell(8).setCellValue("积分抵扣");
        titleRow.createCell(9).setCellValue("尊享码优惠");
        titleRow.createCell(10).setCellValue("订单总额");
        titleRow.createCell(11).setCellValue("买家姓名");
        titleRow.createCell(12).setCellValue("联系电话");
        titleRow.createCell(13).setCellValue("详细地址");
        titleRow.createCell(14).setCellValue("发货时间");
        titleRow.createCell(15).setCellValue("物流信息");
        HSSFCell ce2 = title.createCell((short) 2);
        ce2.setCellStyle(cellStyle); // 样式，居中

            // 取出对象
            OrderModel order2 = order;
            // 创建行
            HSSFRow row = sheet2.createRow(2);
            // 创建单元格并赋值

            // 订单号
            HSSFCell orderNoCell = row.createCell(0);
            orderNoCell.setCellValue(order2.orderNo);

            // 下单时间
            HSSFCell orderTimeCell = row.createCell(1);
            orderTimeCell.setCellValue(order2.createdAtStr);

            // 用户姓名
            HSSFCell nameCell = row.createCell(2);
            if (order2.buyer == null) {
                nameCell.setCellValue("无");
            } else {
                if ("".equals(order2.buyer.nickname)) {
                    nameCell.setCellValue("无");
                }
                nameCell.setCellValue(order2.buyer.nickname);
            }

            // 产品名称
            HSSFCell productCell = row.createCell(3);

            if (order2.orderProducts != null && order2.orderProducts.size() > 0) {
                String orderProductList = "";
                for(ProductModel product : order2.orderProducts){
                    orderProductList += product.name + "\n";
                }
                productCell.setCellValue(orderProductList);
            } else {
                productCell.setCellValue("无");
            }
            // 数量
            HSSFCell quantityCell = row.createCell(4);
            quantityCell.setCellValue(order2.quantity);
            // 单价
            HSSFCell priceCell = row.createCell(5);
			/*priceCell.setCellValue(order2.price);*/
            if (order2.orderProducts != null && order2.orderProducts.size() > 0) {
                String orderProductPriceList = "";
                for(ProductModel product : order2.orderProducts){
                    orderProductPriceList += product.price + "\n";
                }
                priceCell.setCellValue(orderProductPriceList);
            } else {
                priceCell.setCellValue("无");
            }
            // 配送费
            HSSFCell shipFeeCell = row.createCell(6);
            shipFeeCell.setCellValue(order2.shipFee);
            // 商品总额
            HSSFCell productAmountCell = row.createCell(7);
            productAmountCell.setCellValue(order2.productAmount);
            // 积分抵扣
            HSSFCell jifenCell = row.createCell(8);
            jifenCell.setCellValue(order2.jifen);
            // 尊享码优惠
            HSSFCell promotionAmountCell = row.createCell(9);
            promotionAmountCell.setCellValue(order2.promotionAmount);
            // 订单总额
            HSSFCell amountCell = row.createCell(10);
            amountCell.setCellValue(order2.amount);
            // 收货人姓名
            HSSFCell shipNameCell = row.createCell(11);
            shipNameCell.setCellValue(order2.shipName);
            // 电话
            HSSFCell shipPhoneCell = row.createCell(12);
            shipPhoneCell.setCellValue(order2.shipPhone);
            // 详细地址
            HSSFCell shipLocationCell = row.createCell(13);
            shipLocationCell.setCellValue(order2.shipLocation);

            //发货时间
            HSSFCell shipTimeStrCell = row.createCell(14);
            shipTimeStrCell.setCellValue(order2.shipTimeStr);
            //物流信息
            HSSFCell commentCell = row.createCell(15);
            commentCell.setCellValue(order2.comment);


        // 生成文件
        String path = Play.application().path().getPath() + "/public/report/" + fileName;
        File file = new File(path);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            workbook2007.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // response().setHeader("Content-Disposition", "attachment; filename=" +
        // fileName);
        return ok(file);
    }
}