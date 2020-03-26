import java.util.Date;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.Ebean;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import controllers.WeiXinController;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpServiceImpl;
import models.CatalogModel;
import models.FondOutRequestModel;
import models.FoodcommentModel;
import models.InfoModel;
import models.OrderModel;
import models.ProductModel;
import models.PromotionModel;
import models.ShipAreaPriceModel;
import models.ShipInfoModel;
import models.ShoppingCartItemModel;
import models.ShoppingCartModel;
import models.ThemeModel;
import models.UserModel;
import models.EnjoyTheCodeModel;
import models.common.CompanyModel;
import play.Application;
import play.GlobalSettings;
import play.libs.Yaml;

/**
 * Created by yanglu on 14/11/14.
 */
public class Global extends GlobalSettings implements IConst {

	public void onStart(Application app) {
		play.Logger.info(SYSTEM_LAUNCH_INFO);
		InitialData.insert(app);
		WeiXinController.wxInit();
	}

	static class InitialData {
		public static void insert(Application app) {
			if (Ebean.find(ProductModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("products");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);
							play.Logger.info(String.format("load cfg default products %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default products done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}
			if (Ebean.find(UserModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("users");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);

							List<UserModel> list = UserModel.findAll();

							List<ProductModel> products = ProductModel.findAll();
							// user -> product
							if ( products.size() > 0 && list.size() > 0) {
								products.get(0).favoriteUsers.add(list.get(1));
								products.get(1).favoriteUsers.add(list.get(1));
								products.get(2).favoriteUsers.add(list.get(2));
								products.get(3).favoriteUsers.add(list.get(2));

								// product -> user
								list.get(1).favoriteProducts.add(products.get(0));
								list.get(1).favoriteProducts.add(products.get(1));
								list.get(2).favoriteProducts.add(products.get(2));
								list.get(2).favoriteProducts.add(products.get(3));

								Ebean.update(products.get(0));
								Ebean.update(products.get(1));
								Ebean.update(products.get(2));
								Ebean.update(products.get(3));

								Ebean.update(list.get(1));
								Ebean.update(list.get(2));

								Ebean.update(products.get(0));
								Ebean.update(products.get(1));
								Ebean.update(products.get(2));
								Ebean.update(products.get(3));

								for (UserModel user : list) {
									user.resellerCode = UserModel.generateResellerCode();
									try {
										user.resellerCodeImage = WeiXinController
												.generateResellerCodeBarcode(user.resellerCode);
									} catch (Exception e) {
										play.Logger.error(DateUtil.Date2Str(new Date())
												+ " - error on create reseller barcode: " + e.getMessage());
										e.printStackTrace();
									}
									Ebean.update(user);
								}
								play.Logger.info(String.format("load cfg default users %s", defaultObjs.size()));
							}
						}
					}
					play.Logger.info("load cfg default users done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}
			if (Ebean.find(PromotionModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("promotions");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);
							play.Logger.info(String.format("load cfg default promotions %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default promotions done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}

			if (Ebean.find(ShipAreaPriceModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("shipareaprices");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);
							play.Logger.info(String.format("load cfg default shipareaprices %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default shipareaprices done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}
			if (Ebean.find(ShipInfoModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("shipinfos");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);

							List<UserModel> users = UserModel.findAll();

							List<ShipInfoModel> shipinfos = ShipInfoModel.findAll();

							if (users.size() > 0 && shipinfos.size() > 0) {

								// shipinfo -> user
								users.get(0).shipInfos.add(shipinfos.get(0));
								shipinfos.get(0).refUserId = users.get(0).id;
								shipinfos.get(0).user = users.get(0);

								users.get(0).shipInfos.add(shipinfos.get(1));
								shipinfos.get(1).refUserId = users.get(0).id;
								shipinfos.get(1).user = users.get(0);

								users.get(0).shipInfos.add(shipinfos.get(2));
								shipinfos.get(2).refUserId = users.get(0).id;
								shipinfos.get(2).user = users.get(0);

								for (UserModel user : users) {
									Ebean.update(user);
								}
							}

							play.Logger.info(String.format("load cfg default shipinfos %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default shipinfos done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}
			if (Ebean.find(CompanyModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("companys");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);
							play.Logger.info(String.format("load cfg default company %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default company done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}
			if (Ebean.find(CatalogModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("catalogs");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);
							play.Logger.info(String.format("load cfg default catalogs %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default catalogs done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}
			if (Ebean.find(ShoppingCartItemModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("shoppingcartitem");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);

							List<UserModel> users = UserModel.findAll();

							List<ProductModel> products = ProductModel.findAll();

							List<ShoppingCartItemModel> shoppingCartItems = ShoppingCartItemModel.findAll();

							shoppingCartItems.get(0).product = products.get(0);
							products.get(0).cartItems.add(shoppingCartItems.get(0));
							shoppingCartItems.get(1).product = products.get(1);
							products.get(1).cartItems.add(shoppingCartItems.get(1));
							shoppingCartItems.get(2).product = products.get(2);
							products.get(2).cartItems.add(shoppingCartItems.get(2));

							Ebean.update(products.get(0));
							Ebean.update(products.get(1));
							Ebean.update(products.get(2));

							for (ShoppingCartItemModel item : shoppingCartItems) {
								Ebean.update(item);
							}

							ShoppingCartModel cart = new ShoppingCartModel();
							Ebean.save(cart);

							if (users.size() > 0 && shoppingCartItems.size() > 0) {
								for (ShoppingCartItemModel item : shoppingCartItems) {
									cart.items.add(item);
									cart.totalQuantity += item.quantity;
									cart.productAmount += item.quantity * item.product.price;
									cart.refBuyerId = users.get(0).id;
									item.cart = cart;
								}
							}
							for (ShoppingCartItemModel item : shoppingCartItems) {
								Ebean.update(item);
							}
							Ebean.update(cart);

							play.Logger.info(String.format("load cfg default shoppingcart %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default shoppingcart done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}
			if (Ebean.find(ThemeModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("themes");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);

							List<CatalogModel> catalogs = CatalogModel.findAll();

							List<ProductModel> products = ProductModel.findAll();

							List<ThemeModel> themes = ThemeModel.findAll();

							if (catalogs.size() > 0 && products.size() > 0 && themes.size() > 0) {

								// catalog -> product
								products.get(0).catalogs.add(catalogs.get(0));
								products.get(1).catalogs.add(catalogs.get(0));
								products.get(5).catalogs.add(catalogs.get(0));
								products.get(6).catalogs.add(catalogs.get(0));
								products.get(4).catalogs.add(catalogs.get(1));
								products.get(7).catalogs.add(catalogs.get(1));
								products.get(2).catalogs.add(catalogs.get(2));
								products.get(3).catalogs.add(catalogs.get(3));

								// product -> catalog
								catalogs.get(0).products.add(products.get(0));
								catalogs.get(0).products.add(products.get(1));
								catalogs.get(0).products.add(products.get(5));
								catalogs.get(0).products.add(products.get(6));
								catalogs.get(1).products.add(products.get(4));
								catalogs.get(1).products.add(products.get(7));
								catalogs.get(2).products.add(products.get(2));
								catalogs.get(3).products.add(products.get(3));

								// theme -> product
								products.get(0).themes.add(themes.get(0));
								themes.get(0).refProductId = products.get(0).id;
								themes.get(0).product = products.get(0);
								products.get(0).themes.add(themes.get(1));
								themes.get(1).refProductId = products.get(0).id;
								themes.get(1).product = products.get(0);
								products.get(0).themes.add(themes.get(2));
								themes.get(2).refProductId = products.get(0).id;
								themes.get(2).product = products.get(0);

								products.get(1).themes.add(themes.get(3));
								themes.get(3).refProductId = products.get(1).id;
								themes.get(3).product = products.get(1);
								products.get(1).themes.add(themes.get(4));
								themes.get(4).refProductId = products.get(1).id;
								themes.get(4).product = products.get(1);
								products.get(1).themes.add(themes.get(5));
								themes.get(5).refProductId = products.get(1).id;
								themes.get(5).product = products.get(1);

								products.get(2).themes.add(themes.get(6));
								themes.get(6).refProductId = products.get(2).id;
								themes.get(6).product = products.get(2);
								products.get(2).themes.add(themes.get(7));
								themes.get(7).refProductId = products.get(2).id;
								themes.get(7).product = products.get(2);
								products.get(2).themes.add(themes.get(8));
								themes.get(8).refProductId = products.get(2).id;
								themes.get(8).product = products.get(2);

								products.get(3).themes.add(themes.get(9));
								themes.get(9).refProductId = products.get(3).id;
								themes.get(9).product = products.get(3);
								products.get(3).themes.add(themes.get(10));
								themes.get(10).refProductId = products.get(3).id;
								themes.get(10).product = products.get(3);
								products.get(3).themes.add(themes.get(11));
								themes.get(11).refProductId = products.get(3).id;
								themes.get(11).product = products.get(3);

								for (ProductModel product : products) {
									Ebean.update(product);
								}

								Ebean.update(products.get(0));
								Ebean.update(products.get(1));
								Ebean.update(products.get(2));
								Ebean.update(products.get(3));

								Ebean.update(catalogs.get(0));
								Ebean.update(catalogs.get(1));
								Ebean.update(catalogs.get(2));
								Ebean.update(catalogs.get(3));
							}
							play.Logger.info(String.format("load cfg default themes %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default themes done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}
			if (Ebean.find(FoodcommentModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("foodcomments");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);

							List<ProductModel> products = ProductModel.findAll();

							List<UserModel> users = UserModel.findAll();

							List<FoodcommentModel> foodcomments = FoodcommentModel.findAll();

							if (products.size() > 0 && users.size() > 0 && foodcomments.size() > 0) {

								// foodcomments -> product
								products.get(0).foodcomments.add(foodcomments.get(0));
								foodcomments.get(0).refProductId = products.get(0).id;
								foodcomments.get(0).product = products.get(0);

								products.get(1).foodcomments.add(foodcomments.get(1));
								foodcomments.get(1).refProductId = products.get(1).id;
								foodcomments.get(1).product = products.get(1);

								products.get(2).foodcomments.add(foodcomments.get(2));
								foodcomments.get(2).refProductId = products.get(2).id;
								foodcomments.get(2).product = products.get(2);

								products.get(3).foodcomments.add(foodcomments.get(3));
								foodcomments.get(3).refProductId = products.get(3).id;
								foodcomments.get(3).product = products.get(3);

								products.get(4).foodcomments.add(foodcomments.get(4));
								foodcomments.get(4).refProductId = products.get(4).id;
								foodcomments.get(4).product = products.get(4);

								products.get(5).foodcomments.add(foodcomments.get(5));
								foodcomments.get(5).refProductId = products.get(5).id;
								foodcomments.get(5).product = products.get(5);

								products.get(6).foodcomments.add(foodcomments.get(6));
								foodcomments.get(6).refProductId = products.get(6).id;
								foodcomments.get(6).product = products.get(6);

								products.get(7).foodcomments.add(foodcomments.get(7));
								foodcomments.get(7).refProductId = products.get(7).id;
								foodcomments.get(7).product = products.get(7);

								// foodcomments -> user
								users.get(0).foodcomments.add(foodcomments.get(0));
								foodcomments.get(0).refUserId = users.get(0).id;
								foodcomments.get(0).user = users.get(0);

								users.get(0).foodcomments.add(foodcomments.get(1));
								foodcomments.get(1).refUserId = users.get(0).id;
								foodcomments.get(1).user = users.get(0);

								users.get(0).foodcomments.add(foodcomments.get(2));
								foodcomments.get(2).refUserId = users.get(0).id;
								foodcomments.get(2).user = users.get(0);

								users.get(1).foodcomments.add(foodcomments.get(3));
								foodcomments.get(3).refUserId = users.get(1).id;
								foodcomments.get(3).user = users.get(1);

								users.get(1).foodcomments.add(foodcomments.get(4));
								foodcomments.get(4).refUserId = users.get(1).id;
								foodcomments.get(4).user = users.get(1);

								users.get(1).foodcomments.add(foodcomments.get(5));
								foodcomments.get(5).refUserId = users.get(1).id;
								foodcomments.get(5).user = users.get(1);

								users.get(2).foodcomments.add(foodcomments.get(6));
								foodcomments.get(6).refUserId = users.get(2).id;
								foodcomments.get(6).user = users.get(2);

								users.get(2).foodcomments.add(foodcomments.get(7));
								foodcomments.get(7).refUserId = users.get(2).id;
								foodcomments.get(7).user = users.get(2);

								for (ProductModel product : products) {
									Ebean.update(product);
								}
								for (UserModel user : users) {
									Ebean.update(user);
								}
							}
							play.Logger.info(String.format("load cfg default foodcomments %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default foodcomments done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.toString());
				}
			}
			if (Ebean.find(OrderModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("orders");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);

							List<ProductModel> products = ProductModel.findAll();

							List<UserModel> users = UserModel.findAll();

							List<OrderModel> orders = OrderModel.findAll();

							if (products.size() > 0 && users.size() > 0 && orders.size() > 0) {

								// order -> product
								products.get(0).orders.add(orders.get(0));
								products.get(1).orders.add(orders.get(0));
								products.get(2).orders.add(orders.get(1));
								products.get(3).orders.add(orders.get(2));

								// product -> order
								orders.get(0).orderProducts.add(products.get(0));
								orders.get(0).orderProducts.add(products.get(1));
								orders.get(1).orderProducts.add(products.get(2));
								orders.get(2).orderProducts.add(products.get(3));

								// order -> user
								users.get(1).orders.add(orders.get(0));
								orders.get(0).refBuyerId = users.get(1).id;
								orders.get(0).buyer = users.get(1);

								users.get(2).orders.add(orders.get(1));
								orders.get(1).refBuyerId = users.get(2).id;
								orders.get(1).buyer = users.get(2);

								for (UserModel user : users) {
									Ebean.update(user);
								}

								Ebean.update(products.get(0));
								Ebean.update(products.get(1));
								Ebean.update(products.get(2));
								Ebean.update(products.get(3));

								Ebean.update(orders.get(0));
								Ebean.update(orders.get(1));
								Ebean.update(orders.get(2));

							}

							play.Logger.info(String.format("load cfg default orders %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default orders done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}
			if (Ebean.find(InfoModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("infos");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);
							play.Logger.info(String.format("load cfg default infos %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default infos done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}

			if (Ebean.find(EnjoyTheCodeModel.class).findRowCount() == 0) {
				try {
					Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					List<Object> defaultObjs = initData.get("enjoythecode");
					if (defaultObjs != null) {
						if (defaultObjs.size() > 0) {
							Ebean.save(defaultObjs);
							play.Logger.info(String.format("load cfg default enjoythecode %s", defaultObjs.size()));
						}
					}
					play.Logger.info("load cfg default enjoythecode done");
				} catch (Exception ex) {
					play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
				}
			}
		}
	}
}
