package loadbalancer;

import javafx.beans.property.SimpleStringProperty;

public class TagV2 {
	SimpleStringProperty key;
	SimpleStringProperty value;
	public TagV2(SimpleStringProperty key, SimpleStringProperty value) {
		super();
		this.key = key;
		this.value = value;
	}
	public SimpleStringProperty getKey() {
		return key;
	}
	public void setKey(SimpleStringProperty key) {
		this.key = key;
	}
	public SimpleStringProperty getValue() {
		return value;
	}
	public void setValue(SimpleStringProperty value) {
		this.value = value;
	}
	
	
}
