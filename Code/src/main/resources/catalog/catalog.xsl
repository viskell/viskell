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
                        line-height: 1.5;
                    }

                    h1 {
                        margin-top: 0;
                        margin-bottom: 1em;
                        padding: 0.4em;
                        font-size: 2em;
                        background-color: teal;
                        color: white;
                    }
                    h1 small {
                        font-size: 0.65em;
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
                        background-color: #f5f5f5;
                        border: 1px solid #e5e5e5;
                        padding: 1px 3px;
                        margin-right: 1px;
                        line-height: 1.4em;
                        font-family: monospace;
                        color: firebrick;
                    }

                    h1 code, h2 code, h3 code, h4 code {
                        margin: 0;
                        padding: 0;
                        border: none;
                        color: #333;
                        line-height: 1.5;
                    }

                    .brand {
                        color: teal;
                        font-family: serif;
                        font-style: italic;
                        font-weight: bold;
                    }

                    h1.brand {
                        background: transparent;
                        font-size: 3em;
                        margin: 20px;
                    }

                    .content {
                        max-width: 1200px;
                        margin: auto;
                        padding: 20px;
                        background-color: #fff;
                        border: 1px solid #ddd;
                    }

                    .subtle {
                        color: #aaa;
                    }

                    ol.toc {
                        list-style-type: upper-alpha;
                        font-weight: bold;
                    }
                    ol.toc > li {
                        column-break-inside: avoid;
                    }
                    ol.toc > li > h4 {
                        margin: 0;
                        line-height: 2.5;
                    }
                    ol.toc ol {
                        line-height: 1.75;
                        font-weight: normal;
                    }

                    table.data {
                        width: 100%;
                        border: 1px solid #ddd;
                        border-collapse: collapse;
                        font-size: 10pt;
                        display: block;
                        overflow-x: auto;
                    }
                    table.data th, table.data td {
                        border: 1px solid #ddd;
                        padding: 10px;
                        text-align: left;
                        vertical-align: baseline;
                    }
                    table.data th {
                        background-color: #f5f5f5;
                        font-weight: bold;
                    }
                    table.data th h4 {
                        margin: 0;
                    }

                    @media screen and (max-width: 320px) {
                        body {
                            margin: 20px 0;
                        }
                    }
                </style>
            </head>
            <body>
                <h1 class="brand" style="text-align: center;">Viskell</h1>
                <div class="content">
                    <h1>Haskell Catalog <small>v.<xsl:value-of select="/catalog/@version"/></small></h1>
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

                    <h2 id="classes">Type classes</h2>

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

                    <h2 id="functions">Functions</h2>
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
                                            <code><xsl:value-of select="@name"/> :: <xsl:value-of select="@signature"/></code>
                                        </td>
                                        <td>
                                            <p>
                                                <xsl:value-of select="."/>
                                                <xsl:if test=". = ''">
                                                    <em class="subtle">No documentation provided.</em>
                                                </xsl:if>
                                            </p>
                                        </td>
                                    </tr>
                                </xsl:for-each>
                            </xsl:for-each>
                        </tbody>
                    </table>
                </div>
                <p style="text-align: center;">Haskell catalog provided by <span class="brand">Viskell</span> (<a href="https://github.com/wandernauta/Groep10">GitHub</a>).</p>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
