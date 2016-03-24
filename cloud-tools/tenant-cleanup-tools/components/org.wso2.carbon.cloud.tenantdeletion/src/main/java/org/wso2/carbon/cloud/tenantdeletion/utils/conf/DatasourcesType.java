
package org.wso2.carbon.cloud.tenantdeletion.utils.conf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for datasourcesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="datasourcesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="carbon-datasource" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "datasourcesType", propOrder = {
    "carbonDatasource"
})
public class DatasourcesType {

    @XmlElement(name = "carbon-datasource", required = true)
    protected String carbonDatasource;

    /**
     * Gets the value of the carbonDatasource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCarbonDatasource() {
        return carbonDatasource;
    }

    /**
     * Sets the value of the carbonDatasource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCarbonDatasource(String value) {
        this.carbonDatasource = value;
    }

}
