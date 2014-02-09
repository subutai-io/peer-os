<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--
***************************************************************************
*                                                                         *
*  Jay Nixon 2006-10-17                                                   *
*                                                                         *
*  DTD: Red Book Open-Index (open-index-v1-1-2006-10-16.dtd) style sheet  *
*                                                                         *
***************************************************************************
-->
<xsl:output method="html" indent="yes" omit-xml-declaration="yes"/>
<xsl:preserve-space elements="*"/>
<xsl:variable name="startPosition">
	<xsl:choose>
		<xsl:when test="//open-index/volume-label[starts-with(.,'Grant')]">
			<xsl:value-of select = "20"/>
		</xsl:when>
		<xsl:when test="//open-index/volume-label[starts-with(.,'EDC')]">
			<xsl:value-of select = "26"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select = "25"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:variable>
<xsl:variable name="startPosition2">
	<xsl:choose>
		<xsl:when test="//open-index/volume-label[starts-with(.,'Grant')]">
			<xsl:value-of select = "27"/>
		</xsl:when>
		<xsl:when test="//open-index/volume-label[starts-with(.,'EDC')]">
			<xsl:value-of select = "33"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select = "32"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:variable>
<xsl:variable name="startPosition3">
	<xsl:choose>
		<xsl:when test="//open-index/volume-label[starts-with(.,'Grant')]">
			<xsl:value-of select = "32"/>
		</xsl:when>
		<xsl:when test="//open-index/volume-label[starts-with(.,'EDC')]">
			<xsl:value-of select = "38"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select = "37"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:variable>
<xsl:variable name="startTiffPosition">
	<xsl:choose>
		<xsl:when test="//open-index/volume-label[starts-with(.,'Grant')]">
			<xsl:value-of select = "50"/>
		</xsl:when>
		<xsl:when test="//open-index/volume-label[starts-with(.,'EDC')]">
			<xsl:value-of select = "56"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select = "57"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:variable>
<xsl:variable name="startTiffPosition2">
	<xsl:choose>
		<xsl:when test="//open-index/volume-label[starts-with(.,'Grant')]">
			<xsl:value-of select = "57"/>
		</xsl:when>
		<xsl:when test="//open-index/volume-label[starts-with(.,'EDC')]">
			<xsl:value-of select = "63"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select = "64"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:variable>

<xsl:variable name="RBissueDate"          select=".//open-index/main-archive-path" />
<xsl:variable name="RBvolume"             select=".//open-index/volume-label" />
<xsl:variable name="MaxColumns">8</xsl:variable>

<xsl:template match="/">
	<xsl:choose>
		<xsl:when test="//open-index/volume-label[starts-with(.,'Grant')]">
			<xsl:variable name="plants-count"         select="count(//open-index/xml-content/documents/doc/doc-archive/path[substring(.,10,5)='PLANT'])"   />
			<xsl:variable name="reexaminations-count" select="count(//open-index/xml-content/documents/doc/doc-archive/path[substring(.,10,6)='REEXAM'])"  />
			<xsl:variable name="sirs-count"           select="count(//open-index/xml-content/documents/doc/doc-archive/path[substring(.,10,3)='SIR'])"     />
			<xsl:variable name="reissues-count"       select="count(//open-index/xml-content/documents/doc/doc-archive/path[substring(.,10,7)='REISSUE'])" />
			<xsl:variable name="utility-count"        select="count(//open-index/xml-content/documents/doc/doc-archive/path[substring(.,10,4)='UTIL'])"    />
			<xsl:variable name="designs-count"        select="count(//open-index/xml-content/documents/doc/doc-archive/path[substring(.,10,6)='DESIGN'])"  />
			<xsl:variable name="SUPplants-count"         select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder[1]/path[substring(.,15,5)='PLANT'])"   />
			<xsl:variable name="SUPreexaminations-count" select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder[1]/path[substring(.,15,6)='REEXAM'])"  />
			<xsl:variable name="SUPsirs-count"           select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder[1]/path[substring(.,15,3)='SIR'])"     />
			<xsl:variable name="SUPreissues-count"       select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder[1]/path[substring(.,15,7)='REISSUE'])" />
			<xsl:variable name="SUPutility-count"        select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder[1]/path[substring(.,15,4)='UTIL'])"    />
			<xsl:variable name="SUPdesigns-count"        select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder[1]/path[substring(.,15,6)='DESIGN'])"  />
			<xsl:variable name="SUPlengthyTablesTEXT" select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder/file[substring(.,$startPosition,2)='-T' and substring(.,$startPosition2,4)='.TXT'])"  />
			<xsl:variable name="SUPlengthyTablesTIF"  select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder/file[substring(.,$startPosition,2)='-T' and substring(.,$startPosition2,9)='-0001.TIF'])"  />
			<xsl:variable name="SUPlengthyTablesXML"  select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder/file[substring(.,$startPosition,2)='-T' and substring(.,$startPosition2,4)='.XML'])"  />
			<xsl:variable name="SUPlengthyTablesXMLP"  select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder/file[substring(.,$startPosition,2)='-T' and substring(.,$startPosition3,4)='.XML'])"  />
			<xsl:variable name="SUPlengthyTables"     select="$SUPlengthyTablesTEXT + $SUPlengthyTablesTIF + $SUPlengthyTablesXML + $SUPlengthyTablesXMLP"  />
			<xsl:variable name="SUPlengthySequence"      select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder[1]/file[substring(.,$startPosition,11)='-S00001.TXT'])"  />
			<xsl:variable name="NONlengthySequence"      select="count(//open-index/xml-content/documents/doc/doc-archive/file[substring(.,$startPosition,11)='-S00001.XML'])"  />

			<xsl:variable name="math-count"         select="count(//open-index/xml-content/documents/doc/doc-archive/file[substring(.,$startPosition,2)='-M'])"  />
			<xsl:variable name="chemistry-count"    select="count(//open-index/xml-content/documents/doc/doc-archive/file[substring-after(.,'.')='CDX'])"  />
			<xsl:variable name="pullout-count"      select="count(//open-index/xml-content/documents/doc/doc-archive/file[substring(.,$startPosition,2)='-P'])"  />
			<xsl:variable name="TIFF-count"         select="count(//open-index/xml-content/documents/doc/doc-archive/file[substring-after(.,'.')='TIF'])"  />
			<xsl:variable name="drawing-count"      select="$TIFF-count - $pullout-count - $chemistry-count - $math-count"  />

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
				<tr>
				  <td align = "left">CWU Math Count</td>
				  <td align = "right"><xsl:value-of select="format-number($math-count,'##,##,###,###')" /></td>
				  <td align = "right">&#160;</td>
				</tr>
				<tr>
				  <td align = "left">CWU Chemistry Count</td>
				  <td align = "right"><xsl:value-of select="format-number($chemistry-count,'##,##,###,###')" /></td>
				  <td align = "right">&#160;</td>
				</tr>
				<tr>
				  <td align = "left">Page/Character Pullout Count</td>
				  <td align = "right"><xsl:value-of select="format-number($pullout-count,'##,##,###,###')" /></td>
				  <td align = "right">&#160;</td>
				</tr>
				<tr>
				  <td align = "left">Drawing Count</td>
				  <td align = "right"><xsl:value-of select="format-number($drawing-count,'##,##,###,###')" /></td>
				  <td align = "right">&#160;</td>
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
			        <h3>PSIPS Non-Lengthy Sequence Listing Inventory</h3>
			      </center>
			      <table border="0" rules="none" align="center">
				<tr>
				  <td align = "left"><b><u>Patent/Application</u>&#160;&#160;&#160;&#160;&#160;</b></td>
				  <td align = "left"><b><u>Path</u>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</b></td>
				  <td align = "left"><b><u>File Name</u></b></td>
				</tr>
			        <xsl:apply-templates select=".//doc" mode="NONsequence"/>
				<tr>
				  <td align = "left"><b>File Count</b></td>
				  <td align = "left"><xsl:value-of select="format-number($NONlengthySequence,'##,###,###')" /></td>
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
			        <h3>PSIPS Lengthy Sequence Listing Inventory</h3>
			      </center>
			      <table border="0" rules="none" align="center">
				<tr>
				  <td align = "left"><b><u>Patent/Application</u>&#160;&#160;&#160;&#160;&#160;</b></td>
				  <td align = "left"><b><u>Path</u>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</b></td>
				  <td align = "left"><b><u>File Name</u></b></td>
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
				  <td align = "left"><b>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</b></td>
				</tr>
			        <xsl:apply-templates select=".//doc" mode="table"/>
				<tr>
				  <td align = "left"><b>File Count</b></td>
				  <td align = "left"><xsl:value-of select="format-number($SUPlengthyTables,'##,###,###')" /></td>
				</tr>
				<tr>
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
		</xsl:when>
		<xsl:otherwise>
			<xsl:variable name="plants-count"         select="count(//open-index/xml-content/documents/doc/doc-archive/path[substring(.,$startPosition+4,1)='P'])"   />
			<xsl:variable name="utility-count"        select="count(//open-index/xml-content/documents/doc/doc-archive/path[not(substring(.,$startPosition+4,1)='P')])"    />
			<xsl:variable name="SUPplants-count"      select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder[1]/path[substring(.,37,1)='P'])"   />
			<xsl:variable name="SUPutility-count"     select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder[1]/path[not(substring(.,37,1)='P')])"    />

			<xsl:variable name="SUPlengthyTablesTEXT" select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder/file[substring(.,$startPosition,2)='-T' and substring(.,$startPosition2,4)='.TXT'])"  />
			<xsl:variable name="SUPlengthyTablesTIF"  select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder/file[substring(.,$startTiffPosition,2)='-T' and substring(.,$startTiffPosition2,9)='-0001.TIF'])"  />
			<xsl:variable name="SUPlengthyTablesXML"  select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder/file[substring(.,$startPosition,2)='-T' and substring(.,$startPosition2,4)='.XML'])"  />
			<xsl:variable name="SUPlengthyTablesXMLP"  select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder/file[substring(.,$startPosition,2)='-T' and substring(.,$startPosition3,4)='.XML'])"  />
			<xsl:variable name="SUPlengthyTables"     select="$SUPlengthyTablesTEXT + $SUPlengthyTablesTIF + $SUPlengthyTablesXML + $SUPlengthyTablesXMLP"  />

			<xsl:variable name="SUPlengthySequence"   select="count(//open-index/xml-content/documents/doc/doc-supp-folders/doc-supp-folder[1]/file[substring(.,$startPosition,11)='-S00001.TXT'])"  />
			<xsl:variable name="NONlengthySequence"   select="count(//open-index/xml-content/documents/doc/doc-archive/file[substring(.,$startPosition,11)='-S00001.XML'])"  />

			<xsl:variable name="math-count"           select="count(//open-index/xml-content/documents/doc/doc-archive/file[substring(.,$startPosition,2)='-M'])"  />
			<xsl:variable name="chemistry-count"      select="count(//open-index/xml-content/documents/doc/doc-archive/file[substring-after(.,'.')='CDX'])"  />
			<xsl:variable name="pullout-count"        select="count(//open-index/xml-content/documents/doc/doc-archive/file[substring(.,$startPosition,2)='-P'])"  />
			<xsl:variable name="TIFF-count"           select="count(//open-index/xml-content/documents/doc/doc-archive/file[substring-after(.,'.')='TIF'])"  />
			<xsl:variable name="drawing-count"        select="$TIFF-count - $pullout-count - $chemistry-count - $math-count"  />

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
				  <td align = "left"><b>Total</b></td>
				  <td align = "right"><xsl:value-of select="format-number($utility-count+$plants-count,'##,###,###')" /></td>
				  <td align = "right"><xsl:value-of select="format-number($SUPutility-count+$SUPplants-count,'##,###,###')" /></td>
				</tr>
				<tr>
				  <td align = "left">&#160;</td>
				  <td align = "right">&#160;</td>
				  <td align = "right">&#160;</td>
				</tr>
				<tr>
				  <td align = "left">CWU Math Count</td>
				  <td align = "right"><xsl:value-of select="format-number($math-count,'##,##,###,###')" /></td>
				  <td align = "right">&#160;</td>
				</tr>
				<tr>
				  <td align = "left">CWU Chemistry Count</td>
				  <td align = "right"><xsl:value-of select="format-number($chemistry-count,'##,##,###,###')" /></td>
				  <td align = "right">&#160;</td>
				</tr>
				<tr>
				  <td align = "left">Page/Character Pullout Count</td>
				  <td align = "right"><xsl:value-of select="format-number($pullout-count,'##,##,###,###')" /></td>
				  <td align = "right">&#160;</td>
				</tr>
				<tr>
				  <td align = "left">Drawing Count</td>
				  <td align = "right"><xsl:value-of select="format-number($drawing-count,'##,##,###,###')" /></td>
				  <td align = "right">&#160;</td>
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
			        <h3>PSIPS Non-Lengthy Sequence Listing Inventory</h3>
			      </center>
			      <table border="0" rules="none" align="center">
				<tr>
				  <td align = "left"><b><u>Patent/Application</u>&#160;&#160;&#160;&#160;&#160;</b></td>
				  <td align = "left"><b><u>Path</u>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</b></td>
				  <td align = "left"><b><u>File Name</u></b></td>
				</tr>
			        <xsl:apply-templates select=".//doc" mode="NONsequence"/>
				<tr>
				  <td align = "left"><b>File Count</b></td>
				  <td align = "left"><xsl:value-of select="format-number($NONlengthySequence,'##,###,###')" /></td>
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
			        <h3>PSIPS Lengthy Sequence Listing Inventory</h3>
			      </center>
			      <table border="0" rules="none" align="center">
				<tr>
				  <td align = "left"><b><u>Patent/Application</u>&#160;&#160;&#160;&#160;&#160;</b></td>
				  <td align = "left"><b><u>Path</u>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</b></td>
				  <td align = "left"><b><u>File Name</u></b></td>
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
				  <td align = "left"><b>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</b></td>
				</tr>
			        <xsl:apply-templates select=".//doc" mode="table"/>
				<tr>
				  <td align = "left"><b>Lengthy Table Count</b></td>
				  <td align = "left"><xsl:value-of select="format-number($SUPlengthyTables,'##,###,###')" /></td>
				</tr>
				<tr>
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
		</xsl:otherwise>
	</xsl:choose>	
</xsl:template>

<xsl:template match = "doc" mode="NONsequence" >
  <xsl:if test= "./doc-archive/file">
    <xsl:for-each select = "./doc-archive/file">
      <xsl:choose>
        <xsl:when test="substring(.,$startPosition,11)='-S00001.XML'">
	  <tr>
	    <td align = "left">
	    <xsl:value-of select = "ancestor::doc/doc-number" />
	    </td>
	    <td align = "left">
	    <xsl:value-of select = "ancestor::doc-archive/path" />
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

<xsl:template match = "doc" mode="sequence" >
  <xsl:if test= "./doc-supp-folders/doc-supp-folder/file">
    <xsl:for-each select = "./doc-supp-folders/doc-supp-folder/file">
      <xsl:choose>
        <xsl:when test="substring(.,$startPosition,11)='-S00001.TXT'">
	  <tr>
	    <td align = "left">
	    <xsl:value-of select = "ancestor::doc/doc-number" />
	    </td>
	    <td align = "left">
	    <xsl:value-of select = "ancestor::doc-supp-folder/path" />
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
  <xsl:if test= "./doc-supp-folders/doc-supp-folder/file">
    <xsl:for-each select = "./doc-supp-folders/doc-supp-folder/file">
      <xsl:choose>
        <xsl:when test="substring(.,$startPosition,2)='-T' and substring(.,$startPosition2,4)='.TXT' ">
		  <tr>
			<td align = "left">
			<xsl:value-of select = "ancestor::doc/doc-number" />
			</td>
			<td align = "left">
			<b><xsl:text>Path: </xsl:text></b>
			<xsl:value-of select = "ancestor::doc-supp-folder/path" />
			</td>
		  </tr>
		  <tr>
			<td align = "left">&#160;</td>
			<td align = "left">
				<b><xsl:text>File Name: </xsl:text></b>
				<xsl:value-of select = "." />
			</td>
		  </tr>
		  <tr>
			<td align = "left">&#160;</td>
			<td align = "left">
				<b><xsl:text>Type: Text</xsl:text></b>
			</td>
		  </tr>
		  <tr>
			<td align = "left">&#160;</td>
			<td align = "left">
				<b><xsl:text>Original File Name: </xsl:text></b>
				<xsl:value-of select = "./@file-original" />
			</td>
		  </tr>
        </xsl:when>
        <xsl:when test="substring(.,$startPosition,2)='-T' and substring(.,$startPosition2,4)='.XML' ">
		  <tr>
			<td align = "left">
			<xsl:value-of select = "ancestor::doc/doc-number" />
			</td>
			<td align = "left">
			<b><xsl:text>Path: </xsl:text></b>
			<xsl:value-of select = "ancestor::doc-supp-folder/path" />
			</td>
		  </tr>
		  <tr>
			<td align = "left">&#160;</td>
			<td align = "left">
				<b><xsl:text>File Name: </xsl:text></b>
				<xsl:value-of select = "." />
			</td>
		  </tr>
		  <tr>
			<td align = "left">&#160;</td>
			<td align = "left">
				<b><xsl:text>Type: xml</xsl:text></b>
			</td>
		  </tr>		  
		  <xsl:choose>
			  <xsl:when test="./@file-original">
				  <tr>
					<td align = "left">&#160;</td>
					<td align = "left">
						<b><xsl:text>Source: </xsl:text></b>
						<xsl:value-of select = "./@file-original" />
					</td>
				  </tr>
			  </xsl:when>
		  </xsl:choose>
        </xsl:when>
        <xsl:when test="substring(.,$startPosition,2)='-T' and substring(.,$startPosition3,4)='.XML' ">
		  <tr>
			<td align = "left">
			<xsl:value-of select = "ancestor::doc/doc-number" />
			</td>
			<td align = "left">
			<b><xsl:text>Path: </xsl:text></b>
			<xsl:value-of select = "ancestor::doc-supp-folder/path" />
			</td>
		  </tr>
		  <tr>
			<td align = "left">&#160;</td>
			<td align = "left">
				<b><xsl:text>File Name: </xsl:text></b>
				<xsl:value-of select = "." />
			</td>
		  </tr>
		  <tr>
			<td align = "left">&#160;</td>
			<td align = "left">
				<b><xsl:text>Type: xml</xsl:text></b>
			</td>
		  </tr>		  
		  <xsl:choose>
			  <xsl:when test="./@file-original">
				  <tr>
					<td align = "left">&#160;</td>
					<td align = "left">
						<b><xsl:text>Source: </xsl:text></b>
						<xsl:value-of select = "./@file-original" />
					</td>
				  </tr>
			  </xsl:when>
		  </xsl:choose>
        </xsl:when>
        <xsl:when test="substring(.,$startTiffPosition,2)='-T' and substring(.,$startTiffPosition2,9)='-0001.TIF' ">
		  <tr>
			<td align = "left">
			<xsl:value-of select = "ancestor::doc/doc-number" />
			</td>
			<td align = "left">
			<b><xsl:text>Path: </xsl:text></b>
			<xsl:value-of select = "ancestor::doc-supp-folder/path" />
			</td>
		  </tr>
		  <tr>
			<td align = "left">&#160;</td>
			<td align = "left">
				<b><xsl:text>File Name: </xsl:text></b>
				<xsl:value-of select = "." />
			</td>
		  </tr>
		  <tr>
			<td align = "left">&#160;</td>
			<td align = "left">
				<b><xsl:text>Type: TIF</xsl:text></b>
			</td>
		  </tr>
		  <tr>
			<td align = "left">&#160;</td>
			<td align = "left">
				<b><xsl:text>Page Count: </xsl:text></b>
				<!-- <xsl:value-of select="count(ancestor::doc-supp-folder/file[contains(.,'TIF')])"  /> -->
				<xsl:call-template name="countPages" >
					<xsl:with-param name="tableName" select="substring-before(.,'/')" />
				</xsl:call-template>
			</td>
		  </tr>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:if>
</xsl:template>

<xsl:template name="countPages">
	<xsl:param name="tableName" />
	<xsl:value-of select="count(ancestor::doc-supp-folder/file[contains(.,$tableName)])"  />
</xsl:template>

</xsl:stylesheet>
