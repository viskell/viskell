<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/catalog">
        <html>
            <head>
                <title>Functions</title>
                <style type="text/css">
                    body {
                        background: #aaa;
                        color: #333;
                        font-family: sans-serif;
                        font-size: small;
                    }

                    .category {
                        background: white;
                        padding: 20px;
                        padding-bottom: 0;
                        overflow: auto;
                        margin: 20px;
                        box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
                    }

                    .function {
                        background: #eee;
                        border: 1px solid #ddd;
                        padding-left: 10px;
                        margin-bottom: 20px;
                    }

                    .function h2 {
                        font-size: small;
                    }

                    .toc a {
                        text-decoration: none;
                        color: inherit;
                    }
                </style>
            </head>
            <body>
                <div class="content">
                    <div class="toc">
                        <ol>
                            <xsl:for-each select="category">
                                <li>
                                    <a>
                                        <xsl:attribute name="href">#<xsl:value-of select="@name"/></xsl:attribute>
                                        <xsl:value-of select="@name" />
                                    </a>
                                </li>
                            </xsl:for-each>
                        </ol>
                    </div>
                    <xsl:for-each select="category">
                        <div class="category">
                            <xsl:attribute name="id"><xsl:value-of select="@name" /></xsl:attribute>
                            <h1>
                                <xsl:value-of select="position()" />.
                                <xsl:value-of select="@name" />
                            </h1>

                            <xsl:for-each select="function">
                                <div class="function">
                                    <h2><xsl:value-of select="@name" /> :: <xsl:value-of select="@signature" /></h2>
                                    <p>
                                        <xsl:value-of select="." />
                                    </p>
                                </div>
                            </xsl:for-each>
                        </div>
                    </xsl:for-each>
                </div>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
