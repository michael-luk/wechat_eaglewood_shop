package models;

import LyLib.Interfaces.IConst;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "shipareaprices")
public class ShipAreaPriceModel extends Model implements Serializable, IConst {

	@Id
	public Long id;

	public String city; // 城市

	public String zone; // 区域

	public String area; // 地址

	@Column(columnDefinition = "Decimal(10,2)")
	public double shipPrice; // 单位运费

	public String comment;

	public static Finder<Long, ShipAreaPriceModel> find = new Finder(Long.class, ShipAreaPriceModel.class);

	/**
	 * Retrieve all .
	 */
	public static List<ShipAreaPriceModel> findAll() {
		return find.all();
	}

	public String validate() {
		// if (shipPrice == 0) {
		// return "必须要有单位运费";
		// }
		return null;
	}

	@Override
	public String toString() {
		return "ShipAreaPrice [area:" + area + "]";
	}
}