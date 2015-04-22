<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/catalog">
        <html>
            <head>
                <title>Catalog</title>
                <style type="text/css">
                    body {
                        margin: 20px;
                        background-color: #eee;
                        font-family: sans-serif;
                        font-size: 10pt;
                        color: #333;
                    }

                    h1 {
                        margin-top: 0;
                        margin-bottom: 1em;
                        padding: 0.4em;
                        font-size: 2em;
                        background-color: teal;
                        color: white;
                    }
                    h2 {
                        font-size: 1.5em;
                    }
                    h3 {
                        font-size: 1.2em;
                    }
                    h4 {
                        font-size: 1.1em;
                    }

                    a, a:link, a:hover, a:active, a:visited {
                        color: teal;
                        text-decoration: none;
                    }
                    a:hover { text-decoration: underline; }

                    hr {
                        border-collapse: collapse;
                        border: none;
                        border-bottom: 1px solid #ccc;
                    }

                    code {
                        background-color: #eee;
                        border: 1px solid #ddd;
                        padding: 1px 3px;
                        font-family: monospace;
                        color: darkred;
                    }

                    .content {
                        max-width: 800px;
                        margin: auto;
                        padding: 20px;
                        background-color: #fff;
                        border: 1px solid #ddd;
                    }

                    ol.toc {
                        list-style-type: upper-alpha;
                        font-weight: bold;
                    }
                    ol.toc > li {
                        margin-left: 0;
                    }
                    ol.toc ol {
                        font-weight: normal;
                    }

                    table.data {
                        width: 100%;
                        border: 1px solid #ddd;
                        border-collapse: collapse;
                        font-size: 10pt;
                    }
                    table.data th, table.data td {
                        border: 1px solid #ddd;
                        padding: 10px;
                        text-align: left;
                        vertical-align: baseline;
                    }
                    table.data th {
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="content">
                    <h1>Catalog</h1>
                    <h3>Table of contents</h3>
                    <ol class="toc">
                        <li>
                            <h4><a href="#classes">Type classes</a></h4>
                            <ol>
                                <xsl:for-each select="classes/class">
                                    <li>
                                        <a>
                                            <xsl:attribute name="href">#classes-<xsl:value-of select="@name"/></xsl:attribute>
                                            <xsl:value-of select="@name"/>
                                        </a>
                                    </li>
                                </xsl:for-each>
                            </ol>
                        </li>
                        <li>
                            <h4><a href="#functions">Functions</a></h4>
                            <ol>
                                <xsl:for-each select="functions/category">
                                    <li>
                                        <a>
                                            <xsl:attribute name="href">#functions-<xsl:value-of select="@name"/></xsl:attribute>
                                            <xsl:value-of select="@name"/>
                                        </a>
                                    </li>
                                </xsl:for-each>
                            </ol>
                        </li>
                    </ol>

                    <hr/>

                    <h2><a id="classes"/>Type classes</h2>

                    <table class="data">
                        <tbody>
                            <xsl:for-each select="classes/class">
                                <tr>
                                    <th>
                                        <xsl:attribute name="id">classes-<xsl:value-of select="@name" /></xsl:attribute>
                                        <h4><code><xsl:value-of select="@name" /></code></h4>
                                    </th>
                                    <td>
                                        <xsl:for-each select="instance">
                                            <code><xsl:value-of select="@name"/></code>
                                            <xsl:if test="not(position() = last())">, </xsl:if>
                                        </xsl:for-each>
                                    </td>
                                </tr>
                            </xsl:for-each>
                        </tbody>
                    </table>

                    <h2><a id="functions"/>Functions</h2>
                    <p>The functions are separated by category.</p>

                    <table class="data">
                        <tbody>
                            <xsl:for-each select="functions/category">
                                <xsl:for-each select="function">
                                    <tr>
                                        <xsl:if test="position() = 1">
                                            <th>
                                                <xsl:attribute name="id">functions-<xsl:value-of select="../@name" /></xsl:attribute>
                                                <xsl:attribute name="rowspan"><xsl:value-of select="count(../function)"/></xsl:attribute>
                                                <h4><xsl:value-of select="../@name" /></h4>
                                            </th>
                                        </xsl:if>
                                        <td>
                                            <code><xsl:value-of select="@name"/></code>
                                        </td>
                                        <td>
                                            <p><xsl:value-of select="."/></p>
                                        </td>
                                    </tr>
                                </xsl:for-each>
                            </xsl:for-each>
                        </tbody>
                    </table>
                </div>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
