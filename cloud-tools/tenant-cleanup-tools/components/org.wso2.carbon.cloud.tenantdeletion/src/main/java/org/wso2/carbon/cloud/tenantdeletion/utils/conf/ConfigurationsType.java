package org.wso2.carbon.cloud.tenantdeletion.utils.conf;

import javax.xml.bind.annotation.*;

/**
 * <p>Java class for ConfigurationsType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ConfigurationsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="datasources" type="{}datasourcesType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD) @XmlType(name = "ConfigurationsType", propOrder = {
        "datasources" }) @XmlRootElement(name = "Configurations") public class ConfigurationsType {

    @XmlElement(required = true) protected DatasourcesType datasources;

    /**
     * Gets the value of the datasources property.
     *
     * @return possible object is
     * {@link DatasourcesType }
     */
    public DatasourcesType getDatasources() {
        return datasources;
    }

    /**
     * Sets the value of the datasources property.
     *
     * @param value allowed object is
     *              {@link DatasourcesType }
     */
    public void setDatasources(DatasourcesType value) {
        this.datasources = value;
    }

}
