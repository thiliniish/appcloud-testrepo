<?xml version = '1.0' encoding = 'UTF-8'?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
    <xsl:template match="invoice">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master master-name="simpleA4" page-height="29.7cm" page-width="21cm" margin-top="2cm"
                                       margin-bottom="1cm" margin-left="2cm" margin-right="2cm">

                    <fo:region-body/>
                    <fo:region-after region-name="xsl-region-after" extent=".5in"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="simpleA4">
                <fo:static-content flow-name="xsl-region-after">
                    <fo:block font-size="8pt" text-align="center">This is a computer generated document. No signature is
                        required
                        .
                    </fo:block>
                </fo:static-content>
                <fo:flow flow-name="xsl-region-body" font-size="10pt">
                    <fo:block>
                        <fo:float float="right">
                            <fo:block padding="5mm 10mm 0mm 0mm">
                                <fo:block margin="0mm 0mm 0mm 30mm">
                                    <fo:external-graphic
                                            src="http://b.content.wso2.com/newsletter/images/nl-m-wso2-logo.gif"
                                            content-width="40mm"/>&#x00A0;
                                </fo:block>
                                <fo:block linefeed-treatment="preserve" font-size="10pt">
                                    <fo:inline font-weight="bold">Invoice No :</fo:inline>
                                    <xsl:value-of
                                            select="invoiceNumber"/>&#x00A0;
                                    <fo:inline font-weight="bold">Date :</fo:inline>
                                    <xsl:value-of
                                            select="invoiceDate"/>&#x00A0;
                                    <fo:inline font-weight="bold">Client Ref :</fo:inline>
                                    <xsl:value-of
                                            select="customerId"/>&#x00A0;
                                    <fo:inline font-weight="bold">Email :</fo:inline>
                                    <xsl:value-of
                                            select="email"/>&#x00A0;
                                    <fo:inline font-weight="bold"> <xsl:value-of
                                            select="customId1"/> </fo:inline>
                                    <xsl:value-of
                                            select="customData1"/>&#x00A0;
                                    <fo:inline font-weight="bold"><xsl:value-of
                                            select="customId2"/> </fo:inline>
                                    <xsl:value-of
                                            select="customData2"/>&#x00A0;
                                    <fo:inline font-weight="bold"><xsl:value-of
                                            select="customId3"/> </fo:inline>
                                    <xsl:value-of
                                            select="customData3"/>&#x00A0;
                                    <fo:inline font-weight="bold"><xsl:value-of
                                            select="customId4"/> </fo:inline>
                                    <xsl:value-of
                                            select="customData4"/>&#x00A0;
                                    <fo:inline font-weight="bold"><xsl:value-of
                                            select="customId5"/> </fo:inline>
                                    <xsl:value-of
                                            select="customData5"/>
                                    &#x00A0;
                                </fo:block>
                            </fo:block>
                        </fo:float>
                        <fo:block linefeed-treatment="preserve" font-size="10pt">
                            <fo:inline font-weight="bold">WSO2, Inc.</fo:inline>
                            787 Castro Street
                            Mountain View, CA 94041
                            United States
                            Tel.: 408 754 7388
                            Email: Billing@wso2.com
                            &#x00A0;
                        </fo:block>
                    </fo:block>
                    <fo:block linefeed-treatment="preserve" font-size="10pt">
                        <fo:inline font-weight="bold">Attn: Accounts Payable</fo:inline>&#x00A0;
                        <xsl:value-of select="organization"/>&#x00A0;
                        <xsl:value-of select="addressLine1"/>&#160;<xsl:value-of select="addressLine2"/>&#x00A0;
                        <xsl:value-of select="addressCity"/>,&#160;<xsl:value-of
                            select="addressState"/>&#160;<xsl:value-of select="addressZip"/>&#x00A0;
                        <xsl:value-of select="addressCountry"/>&#x00A0;
                        &#x00A0;
                        &#x00A0;
                    </fo:block>
                    <fo:block font-size="10pt" padding="5mm 0mm 0mm 0mm">
                        <fo:table>
                            <fo:table-column column-width="5cm"/>
                            <fo:table-column column-width="5cm"/>
                            <fo:table-column column-width="4.5cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-header>
                                <fo:table-row background-color="rgb(252,102,33)" height="0.4cm" text-align="center"
                                              color="white">
                                    <fo:table-cell border-width="0.2mm" border-style="solid" border-color="white"
                                                   padding="2mm">
                                        <fo:block>Rate Plan Name</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell border-width="0.2mm" border-style="solid" border-color="white"
                                                   padding="2mm">
                                        <fo:block>Charge Name</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell border-width="0.2mm" border-style="solid" border-color="white"
                                                   padding="2mm">
                                        <fo:block>Period</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell border-width="0.2mm" border-style="solid" border-color="white"
                                                   padding="2mm">
                                        <fo:block>Value (USD)</fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <xsl:apply-templates select="invoiceItems/invoiceItem"/>
                            </fo:table-header>

                            <fo:table-body>
                                <fo:table-row height="0.4cm" text-align="center">
                                    <fo:table-cell number-columns-spanned="2" padding="2mm">
                                        <fo:block></fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell background-color="rgb(253, 152, 64)" padding="2mm">
                                        <fo:block>TOTAL</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell background-color="rgb(253, 152, 64)" padding="2mm">
                                        <fo:block>
                                            <xsl:value-of select="amount"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>

                    <fo:block>
                        Terms
                        <fo:list-block provisional-distance-between-starts="10mm" end-indent="10mm" start-indent="10mm">
                            <fo:list-item>
                                <fo:list-item-label end-indent="label-end()">
                                    <fo:block>
                                        •
                                    </fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()">
                                    <fo:block>
                                        Payment terms: Due Upon Receipt
                                    </fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                            <fo:list-item>
                                <fo:list-item-label end-indent="label-end()">
                                    <fo:block>
                                        •
                                    </fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()">
                                    <fo:block>
                                        Late payment may incur interest at the rate of 1.5% per month
                                    </fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                        </fo:list-block>

                        <fo:float float="right">
                            <fo:block padding="mm 10mm 10mm 10mm" linefeed-treatment="preserve">
                                &#x00A0;
                                &#x00A0;
                                <fo:inline font-weight="bold">Padmika Dissanalke</fo:inline>
                                VP – Finance
                                Authorized Signatory
                                WS02, Inc.
                            </fo:block>
                        </fo:float>
                    </fo:block>

                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template match="invoiceItems/invoiceItem">
        <fo:table-row height="1cm" text-align="center">
            <fo:table-cell border-width="0.3mm" border-after-style="solid" border-after-color="orange" padding="2mm">
                <fo:block>
                    <xsl:value-of select="subscriptionName"/>
                </fo:block>
            </fo:table-cell>

            <fo:table-cell border-width="0.3mm" border-after-style="solid" border-after-color="orange" padding="2mm">
                <fo:block>
                    <xsl:value-of select="chargeName"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell border-width="0.3mm" border-after-style="solid" border-after-color="orange" padding="2mm">
                <fo:block>
                    <xsl:value-of select="servicePeriod"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell border-width="0.3mm" border-after-style="solid" border-after-color="orange" padding="2mm">
                <fo:block>
                    <xsl:value-of select="amount"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

</xsl:stylesheet>
