<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--
*********************************************************************
*                                                                   *
*  Jay Nixon 2003-09-15                                             *
*                                                                   *
*  DTD: Red Book Open-Index (open-index.dtd) style sheet            *
*                                                                   *
*********************************************************************
-->
<xsl:output method="html" indent="yes" omit-xml-declaration="yes"/>
<xsl:preserve-space elements="*"/>
<xsl:variable name="startPosition">
<!--
	use 25 for pregrant and 20 for grant
-->
	<xsl:choose>
		<xsl:when test="//open-index/volume-label[starts-with(.,'Grant')]">
			<xsl:value-of select = "20"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select = "25"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:variable>

<xsl:variable name="RBissueDate"          select=".//open-index/main-path" />
<xsl:variable name="RBvolume"             select=".//open-index/volume-label" />
<xsl:variable name="plants-count"         select="count(//open-index/xml-content/documents/doc/m-path[substring(.,10,5)='PLANT'])"   />
<xsl:variable name="reexaminations-count" select="count(//open-index/xml-content/documents/doc/m-path[substring(.,10,6)='REEXAM'])"  />
<xsl:variable name="sirs-count"           select="count(//open-index/xml-content/documents/doc/m-path[substring(.,10,3)='SIR'])"     />
<xsl:variable name="reissues-count"       select="count(//open-index/xml-content/documents/doc/m-path[substring(.,10,7)='REISSUE'])" />
<xsl:variable name="utility-count"        select="count(//open-index/xml-content/documents/doc/m-path[substring(.,10,4)='UTIL'])"    />
<xsl:variable name="designs-count"        select="count(//open-index/xml-content/documents/doc/m-path[substring(.,10,6)='DESIGN'])"  />
<xsl:variable name="SUPplants-count"         select="count(//open-index/xml-content/documents/doc/s-path[substring(.,15,5)='PLANT'])"   />
<xsl:variable name="SUPreexaminations-count" select="count(//open-index/xml-content/documents/doc/s-path[substring(.,15,6)='REEXAM'])"  />
<xsl:variable name="SUPsirs-count"           select="count(//open-index/xml-content/documents/doc/s-path[substring(.,15,3)='SIR'])"     />
<xsl:variable name="SUPreissues-count"       select="count(//open-index/xml-content/documents/doc/s-path[substring(.,15,7)='REISSUE'])" />
<xsl:variable name="SUPutility-count"        select="count(//open-index/xml-content/documents/doc/s-path[substring(.,15,4)='UTIL'])"    />
<xsl:variable name="SUPdesigns-count"        select="count(//open-index/xml-content/documents/doc/s-path[substring(.,15,6)='DESIGN'])"  />
<!--  for PreGrant:
<xsl:variable name="SUPlengthyTables"        select="count(//open-index/xml-content/documents/doc/s-file[substring(.,25,2)='-T'])"  />
<xsl:variable name="SUPlengthySequence"      select="count(//open-index/xml-content/documents/doc/s-file[substring(.,25,9)='-SUPP.SEQ'])"  
-->
<xsl:variable name="SUPlengthyTables"        select="count(//open-index/xml-content/documents/doc/s-file[substring(.,$startPosition,2)='-T'])"  />
<xsl:variable name="SUPlengthySequence"      select="count(//open-index/xml-content/documents/doc/s-file[substring(.,$startPosition,9)='-SUPP.SEQ'])"  />
<xsl:variable name="MaxColumns">8</xsl:variable>
 
<xsl:template match="/">
  <html>
  <head>
  </head>
  <body>
    <basefont face="Times New Roman, Times New Roman, Times New Roman " size="2">

      <center>
        <h3>UNITED STATES PATENT AND TRADEMARK OFFICE</h3>
        <h3>
        <xsl:value-of select='$RBvolume' />
        </h3>
        <h3>Issue Date 
        <xsl:value-of select='$RBissueDate' />
        </h3>
      </center>
      <table border="0" rules="none" align="center">
	<tr>
	  <td align = "left"></td>
	  <td align = "right"><b>Patent/Application</b></td>
	  <td align = "right"><b>Patent/Application</b></td>
	</tr>
	<tr>
	  <td align = "left"></td>
	  <td align = "right"><b>Issue</b></td>
	  <td align = "right"><b>Supplemental</b></td>
	</tr>
	<tr>
	  <td align = "left"><b><u>Patent Type &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</u></b></td>
	  <td align = "right"><b>&#160;&#160;&#160;&#160;<u>&#160;&#160;&#160;&#160;&#160;Count</u></b></td>
	  <td align = "right"><b>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;<u>&#160;&#160;&#160;&#160;&#160;Count</u></b></td>
	</tr>
	<tr>
	  <td align = "left"><b>Reexaminations</b></td>
	  <td align = "right"><xsl:value-of select="format-number($reexaminations-count,'##,###,###')" /></td>
	  <td align = "right"><xsl:value-of select="format-number($SUPreexaminations-count,'##,###,###')" /></td>
	</tr>
	<tr>
	  <td align = "left"><b>Statuatory Invention Registrations</b></td>
	  <td align = "right"><xsl:value-of select="format-number($sirs-count,'##,###,###')" /></td>
	  <td align = "right"><xsl:value-of select="format-number($SUPsirs-count,'##,###,###')" /></td>
	</tr>
	<tr>
	  <td align = "left"><b>Reissue Patents</b></td>
	  <td align = "right"><xsl:value-of select="format-number($reissues-count,'##,###,###')" /></td>
	  <td align = "right"><xsl:value-of select="format-number($SUPreissues-count,'##,###,###')" /></td>
	</tr>
	<tr>
	  <td align = "left"><b>Plant Patents</b></td>
	  <td align = "right"><xsl:value-of select="format-number($plants-count,'##,###,###')" /></td>
	  <td align = "right"><xsl:value-of select="format-number($SUPplants-count,'##,###,###')" /></td>
	</tr>
	<tr>
	  <td align = "left"><b>Utility Patents - General &amp; Mechanical</b></td>
	  <td align = "right"><xsl:value-of select="format-number($utility-count,'##,###,###')" /></td>
	  <td align = "right"><xsl:value-of select="format-number($SUPutility-count,'##,###,###')" /></td>
	</tr>
	<tr>
	  <td align = "left"><b>Design Patents</b></td>
	  <td align = "right"><xsl:value-of select="format-number($designs-count,'##,###,###')" /></td>
	  <td align = "right"><xsl:value-of select="format-number($SUPdesigns-count,'##,###,###')" /></td>
	</tr>
	<tr>
	  <td align = "left"><b>Total</b></td>
	  <td align = "right"><xsl:value-of select="format-number($utility-count+$designs-count+$plants-count+$reissues-count+$sirs-count+$reexaminations-count,'##,###,###')" /></td>
	  <td align = "right"><xsl:value-of select="format-number($SUPutility-count+$SUPdesigns-count+$SUPplants-count+$SUPreissues-count+$SUPsirs-count+$SUPreexaminations-count,'##,###,###')" /></td>
	</tr>
	<tr>
	  <td align = "left">&#160;</td>
	  <td align = "right">&#160;</td>
	  <td align = "right">&#160;</td>
	</tr>
      </table>
      <br/>
      <br/>
      <center>
        <h3>PSIPS Lengthy Sequence Listing Inventory</h3>
      </center>
      <table border="0" rules="none" align="center">
	<tr>
	  <td align = "left"><b><u>Patent/Application</u>&#160;&#160;&#160;&#160;&#160;</b></td>
	  <td align = "left"><b><u>Path</u>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</b></td>
	  <td align = "left"><b><u>Filename</u></b></td>
	</tr>
        <xsl:apply-templates select=".//doc" mode="sequence"/>
	<tr>
	  <td align = "left"><b>File Count</b></td>
	  <td align = "left"><xsl:value-of select="format-number($SUPlengthySequence,'##,###,###')" /></td>
	  <td align = "left">&#160;</td>
	</tr>
	<tr>
	  <td align = "left">&#160;</td>
	  <td align = "left">&#160;</td>
	  <td align = "left">&#160;</td>
	</tr>
      </table>
      <br/>
      <br/>
      <center>
        <h3>PSIPS Lengthy Tables Inventory</h3>
      </center>
      <table border="0" rules="none" align="center">
	<tr>
	  <td align = "left"><b><u>Patent/Application</u>&#160;&#160;&#160;&#160;&#160;</b></td>
	  <td align = "left"><b><u>Path</u>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</b></td>
	  <td align = "left"><b><u>Filename</u></b></td>
	</tr>
        <xsl:apply-templates select=".//doc" mode="table"/>
	<tr>
	  <td align = "left"><b>File Count</b></td>
	  <td align = "left"><xsl:value-of select="format-number($SUPlengthyTables,'##,###,###')" /></td>
	  <td align = "left">&#160;</td>
	</tr>
	<tr>
	  <td align = "left">&#160;</td>
	  <td align = "left">&#160;</td>
	  <td align = "left">&#160;</td>
	</tr>
      </table>
      <table border="0" rules="none" align="center">
	<tr><td colspan="{$MaxColumns}">&#160;</td></tr>
	<tr><td colspan="{$MaxColumns}" align="center">* * * * * End of Red Book Open Index * * * * *</td></tr>
      </table>
    </basefont>
  </body>
  </html>
</xsl:template>

<xsl:template match = "doc" mode="sequence" >
  <xsl:if test= "./s-file">
    <xsl:for-each select = "./s-file">
      <xsl:choose>
        <xsl:when test="substring(.,$startPosition,9)='-SUPP.SEQ'">
	  <tr>
	    <td align = "left">
	    <xsl:value-of select = "ancestor::doc/doc-number" />
	    </td>
	    <td align = "left">
	    <xsl:value-of select = "ancestor::doc/s-path" />
	    </td>
	    <td align = "right">
            <xsl:value-of select = "." />
	    </td>
	  </tr>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:if>
</xsl:template>

<xsl:template match = "doc" mode="table" >
  <xsl:if test= "./s-file">
    <xsl:for-each select = "./s-file">
      <xsl:choose>
        <xsl:when test="substring(.,$startPosition,2)='-T'">
	  <tr>
	    <td align = "left">
	    <xsl:value-of select = "ancestor::doc/doc-number" />
	    </td>
	    <td align = "left">
	    <xsl:value-of select = "ancestor::doc/s-path" />
	    </td>
	    <td align = "right">
            <xsl:value-of select = "." />
	    </td>
	  </tr>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:if>
</xsl:template>
</xsl:stylesheet>
