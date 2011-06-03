<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:lxslt="http://xml.apache.org/xslt" xmlns:redirect="http://xml.apache.org/xalan/redirect" xmlns:stringutils="xalan://org.apache.tools.ant.util.StringUtils" extension-element-prefixes="redirect">
    <xsl:output method="html" encoding="utf-8" indent="yes" />
    <xsl:decimal-format decimal-separator="." grouping-separator="," />
    <!-- Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); 
        you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES 
        OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License. -->

    <!-- Sample stylesheet to be used with Ant JUnitReport output. It creates a set of HTML files a la javadoc where you can browse easily through all packages and classes. -->
    <xsl:param name="output.dir" select="'.'" />
    <xsl:param name="TITLE">
        Unit Test Results
    </xsl:param>


    <xsl:template match="testsuites">

        <!-- create the all.html -->
        <redirect:write file="{$output.dir}/all.html">
            <xsl:call-template name="all.html" />
        </redirect:write>

        <!-- create the failed.html this will include tests with errors as well as failures -->
        <redirect:write file="{$output.dir}/failed.html">
            <xsl:call-template name="failed.html" />
        </redirect:write>

        <!-- create the overview.html -->
        <redirect:write file="{$output.dir}/index.html">
            <xsl:call-template name="index.html" />
        </redirect:write>

        <!-- generate individual reports per test case -->
        <xsl:for-each select="./testsuite[not(./@package = preceding-sibling::testsuite/@package)]">
            <xsl:call-template name="package">
                <xsl:with-param name="name" select="@package" />
            </xsl:call-template>
        </xsl:for-each>

        <!-- create the stylesheet.css -->
        <redirect:write file="{$output.dir}/stylesheet.css">
            <xsl:call-template name="stylesheet.css" />
        </redirect:write>
        
    </xsl:template>


    <!-- Process each package -->
    <xsl:template name="package">
        <xsl:param name="name" />
        <xsl:variable name="package.dir">
            <xsl:if test="not($name = '')">
                <xsl:value-of select="translate($name,'.','/')" />
            </xsl:if>
            <xsl:if test="$name = ''">
                <xsl:value-of select="$name" />
            </xsl:if>
        </xsl:variable>



        <xsl:for-each select="/testsuites/testsuite[@package = $name]">
            <xsl:if test="$package.dir = ''">
                <redirect:write file="{$output.dir}/{@id}_{@name}.html">
                    <xsl:apply-templates select="." mode="testsuite.page" />
                </redirect:write>
            </xsl:if>
            <xsl:if test="not($package.dir = '')">
                <redirect:write file="{$output.dir}/{$package.dir}/{@id}_{@name}.html">
                    <xsl:apply-templates select="." mode="testsuite.page" />
                </redirect:write>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <!-- One file per test suite / class -->
    <xsl:template match="testsuite" name="testsuite" mode="testsuite.page">
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
        <html>
            <head>
                <title>
                    Test - <xsl:value-of select="@name" />
                </title>
                <xsl:call-template name="create.resource.links">
                    <xsl:with-param name="package.name" select="@package" />
                </xsl:call-template>
            </head>
            <body>

                <div class="container container_8" id="report">
                    
                    <!-- The Grails logo and page header-->
                    <div class="grid_6">
                        <xsl:call-template name="create.logo.link">
                            <xsl:with-param name="package.name" select="@package" />
                        </xsl:call-template>
                        
                        <h1><xsl:value-of select="@name" /></h1>
                        <h2>Package: <xsl:value-of select="@package" /></h2>
                    </div>
                    
                    <!-- The navigation links in the upper right corner -->
                    <div class="grid_2">
                        <xsl:call-template name="navigation.links">
	                        <xsl:with-param name="package.name" select="@package" />
	                    </xsl:call-template>
                    </div>

                    <div class="clear"></div>
                    
                    <xsl:apply-templates select="." mode="summary">
                            <xsl:sort select="@errors + @failures" data-type="number" order="descending" />
                            <xsl:sort select="@name" />
                    </xsl:apply-templates>
                    
                    <div class="clear"></div>	                    
                </div>

                <xsl:call-template name="output.parser.js" />

            </body>
        </html>
    </xsl:template>


    <!-- This will produce a large file containing failed (including errors) tests -->
    <xsl:template name="failed.html" match="testsuites" mode="all.tests">
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
        <html>
            <head>
                <title><xsl:value-of select="$TITLE" /> - Failed tests</title>
                <link href="stylesheet.css" rel="stylesheet" type="text/css" />
            </head>
            <body>

                <div id="report" class="container container_8">
                    <div class="grid_6 alpha">
                        <div class="grailslogo"></div>
                        <h1><xsl:value-of select="$TITLE" /> - Failed tests</h1>

                        <p class="intro">
                            <xsl:call-template name="test.count.summary">
                                <xsl:with-param name="tests" select="sum(testsuite/@tests)" />
                                <xsl:with-param name="errors" select="sum(testsuite/@errors)" />
                                <xsl:with-param name="failures" select="sum(testsuite/@failures)" />
                            </xsl:call-template>
                        </p>
                    </div>
                                        
                    <!-- Page navigation links -->
                    <div class="grid_2 omega">
                        <xsl:call-template name="navigation.links">
	                        <xsl:with-param name="package.name" select="''" />
	                    </xsl:call-template>
                    </div>

                    <div class="clear"></div>

                    <xsl:apply-templates select="testsuite[@errors &gt; 0 or @failures &gt; 0]" mode="summary">
                        <xsl:sort select="@errors + @failures" data-type="number" order="descending" />
                        <xsl:sort select="@name" />
                    </xsl:apply-templates>
                    
                    <div class="clear"></div>
                </div>

                <xsl:call-template name="output.parser.js" />
            </body>
        </html>
    </xsl:template>

    <xsl:template name="all.html" match="testsuites" mode="all.tests">
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
        <html>
            <head>
                <title><xsl:value-of select="$TITLE" /> - All tests</title>
                <link href="stylesheet.css" rel="stylesheet" type="text/css" />
            </head>
            <body>

                <div id="report" class="container container_8">
                    
                    <!-- Logo and page header -->
                    <div class="grid_6 alpha">
                        <div class="grailslogo"></div>
                        
                        <h1><xsl:value-of select="$TITLE" /> - All tests </h1>

                        <p class="intro">
                            <xsl:call-template name="test.count.summary">
                                <xsl:with-param name="tests" select="sum(testsuite/@tests)" />
                                <xsl:with-param name="errors" select="sum(testsuite/@errors)" />
                                <xsl:with-param name="failures" select="sum(testsuite/@failures)" />
                            </xsl:call-template>
                        </p>
                    </div>
                    
                    <!-- Page navigation links -->
                    <div class="grid_2 omega">
	                    <xsl:call-template name="navigation.links">
	                        <xsl:with-param name="package.name" select="''" />
	                    </xsl:call-template>
                    </div>
                    
                    <div class="clear"></div>

                    <xsl:apply-templates select="testsuite" mode="summary">
                        <xsl:sort select="@errors + @failures" data-type="number" order="descending" />
                        <xsl:sort select="@name" />
                    </xsl:apply-templates>
                    
                    <div class="clear"></div>
                </div>

                <xsl:call-template name="output.parser.js" />
            </body>
        </html>
    </xsl:template>


    <!-- Produces a file with a package / test case summary with links to more detailed per-test case reports. -->
    <xsl:template name="index.html" match="testsuites" mode="all.tests">
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
        <html>
            <head>
                <title><xsl:value-of select="$TITLE" /> - Package summary</title>
                <link href="stylesheet.css" rel="stylesheet" type="text/css" />
            </head>
            <body>
            
                <div id="report" class="container container_8">
                    <div class="grid_6 alpha">
                        <div class="grailslogo"></div>
                        <h1><xsl:value-of select="$TITLE" />- Summary </h1>

                        <p class="intro">
                            <xsl:call-template name="test.count.summary">
                                <xsl:with-param name="tests" select="sum(testsuite/@tests)" />
                                <xsl:with-param name="errors" select="sum(testsuite/@errors)" />
                                <xsl:with-param name="failures" select="sum(testsuite/@failures)" />
                            </xsl:call-template>
                        </p>
                    </div>
                    
                    <div class="grid_2 omega">
                        <xsl:call-template name="navigation.links">
                            <xsl:with-param name="package.name" select="''" />
                        </xsl:call-template>
                    </div>

                    <div class="clear"></div>

                    <xsl:for-each select="./testsuite[not(./@package = preceding-sibling::testsuite/@package)]">
                        <xsl:sort select="@errors + @failures" data-type="number" order="descending" />
                        <xsl:sort select="../@name" />

                        <xsl:call-template name="packages.overview">
                            <xsl:with-param name="packageName" select="@package" />
                        </xsl:call-template>
                    </xsl:for-each>
                    
                    <div class="clear"></div>
                </div>

            </body>
        </html>
    </xsl:template>


    <!-- A list of all packages and their test cases -->
    <xsl:template name="packages.overview">
        <xsl:param name="packageName" />

        <xsl:variable name="sumTime" select="sum(/testsuites/testsuite[@package = $packageName]/@time)" />
        <xsl:variable name="testCount" select="sum(/testsuites/testsuite[@package = $packageName]/@tests)" />
        <xsl:variable name="errorCount" select="sum(/testsuites/testsuite[@package = $packageName]/@errors)" />
        <xsl:variable name="failureCount" select="sum(/testsuites/testsuite[@package = $packageName]/@failures)" />
        <xsl:variable name="successCount" select="$testCount - $errorCount - $failureCount" />

        <xsl:variable name="cssclass">
            <xsl:choose>
                <xsl:when test="$failureCount &gt; 0 and $errorCount = 0">failure</xsl:when>
                <xsl:when test="$errorCount &gt; 0">error</xsl:when>
                <xsl:otherwise>success</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <div>
            <xsl:attribute name="class">testsuite <xsl:value-of select="$cssclass" /></xsl:attribute>

            <div class="header">
                <h2><xsl:value-of select="$packageName" /></h2>
                <h3>
                    <xsl:call-template name="test.count.summary">
                        <xsl:with-param name="tests" select="$testCount" />
                        <xsl:with-param name="errors" select="$errorCount" />
                        <xsl:with-param name="failures" select="$failureCount" />
                    </xsl:call-template>
                </h3>
            </div>
            
            <ul class="clearfix">
                <xsl:for-each select="/testsuites/testsuite[@package = $packageName]">
                    <xsl:sort select="@name" />

                    <xsl:variable name="testcaseCssClass">
                        <xsl:choose>
                            <xsl:when test="count(testcase/error) &gt; 0">error</xsl:when>
                            <xsl:when test="count(testcase/failure) &gt; 0">failure</xsl:when>
                            <xsl:otherwise>success</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <li>
                        <xsl:attribute name="class">packagelink <xsl:value-of select="$testcaseCssClass" /></xsl:attribute>

                        <a>
                            <xsl:variable name="package.name" select="@package" />

                            <xsl:attribute name="href">
                            <xsl:if test="not($package.name='')">
                                <xsl:value-of select="translate($package.name,'.','/')" /><xsl:text>/</xsl:text>
                            </xsl:if><xsl:value-of select="@id" />_<xsl:value-of select="@name" /><xsl:text>.html</xsl:text>
                        </xsl:attribute>

                            <xsl:attribute name="title"><xsl:value-of select="@tests" /> tests executed in <xsl:value-of select="@time" /> seconds.</xsl:attribute>

                            <span>
                                <xsl:attribute name="class">icon <xsl:value-of select="$testcaseCssClass" /></xsl:attribute>
                            </span>
                            <xsl:value-of select="@name" />
                        </a>
                    </li>
                </xsl:for-each>
            </ul>
        </div>
    </xsl:template>


    <!-- Writes the test summary -->
    <xsl:template match="testsuite" mode="summary">
        <xsl:variable name="cssclass">
            <xsl:choose>
                <xsl:when test="@failures &gt; 0 and @errors = 0">failure</xsl:when>
                <xsl:when test="@errors &gt; 0">error</xsl:when>
                <xsl:otherwise>success</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <div>
            <xsl:attribute name="class">testsuite <xsl:value-of select="$cssclass" /></xsl:attribute>

            <div class="header">
                <h2><xsl:value-of select="@name" /></h2>
                <h3>
                    <xsl:call-template name="test.count.summary">
                        <xsl:with-param name="tests" select="@tests" />
                        <xsl:with-param name="errors" select="@errors" />
                        <xsl:with-param name="failures" select="@failures" />
                    </xsl:call-template>
                </h3>
            </div>

            <xsl:apply-templates select="testcase" mode="tableline">
            </xsl:apply-templates>

            <div class="clearfix output footer">
                <div class="sysout">
                    <h2>Standard output</h2>
                    <pre class="stdout">
                        <xsl:value-of select="system-out" />
                    </pre>
                </div>
                <div class="syserr">
                    <h2>System error</h2>
                    <pre class="syserr">
                        <xsl:value-of select="system-err" />
                    </pre>
                </div>
            </div>
            <div class="clear"></div>
        </div>
    </xsl:template>
    
    <!-- Test method -->
    <xsl:template match="testcase" mode="tableline">
        <xsl:variable name="cssclass">
            <xsl:choose>
                <xsl:when test="count(error) &gt; 0">error</xsl:when>
                <xsl:when test="count(failure) &gt; 0">failure</xsl:when>
                <xsl:otherwise>success</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <div class="grid_8">
            <xsl:attribute name="data-name"><xsl:value-of select="@name" /></xsl:attribute>
            <xsl:attribute name="class">testcase clearfix <xsl:value-of select="$cssclass" /> grid_8 alpha omega</xsl:attribute>

            <div class="grid_3 alpha">
                <p>
                    <span>
                        <xsl:attribute name="class">icon <xsl:value-of select="$cssclass" /></xsl:attribute>
                    </span>
                    <b>
                        <xsl:attribute name="class">testname message <xsl:value-of select="$cssclass" /></xsl:attribute>
                        <xsl:value-of select="@name" />
                    </b>
                </p>

                <p class="summary">Executed in <xsl:value-of select="@time" /> seconds.</p>
            </div>
    
            <div class="grid_5 omega outputinfo">
                <xsl:apply-templates select="failure | error" mode="testcase.details" />
            </div>
            
            <div class="clear"></div>
        </div>
    </xsl:template>

    <!-- Test failure -->
    <xsl:template match="failure | error" mode="testcase.details">
        <div class="details">
            <p>
                <b class="message">
                    <xsl:value-of select="@message" />
                </b>
            </p>
            <pre>
                <xsl:value-of select="." />
            </pre>
        </div>
    </xsl:template>


    <!-- Test count summary, the number of executed tests, errors and failures -->
    <xsl:template name="test.count.summary">
        <xsl:param name="tests" />
        <xsl:param name="errors" />
        <xsl:param name="failures" />

        <xsl:choose>
            <xsl:when test="$tests = 0">
                No tests executed.
            </xsl:when>
            <xsl:otherwise>

                <!-- Test count -->
                <xsl:choose>
                    <xsl:when test="$tests = 1">
                        A single test executed
                    </xsl:when>
                    <xsl:otherwise>
                        Executed
                        <xsl:value-of select="$tests" />
                        tests
                    </xsl:otherwise>
                </xsl:choose>

                <!-- Error / failure count -->
                <xsl:choose>
                    <xsl:when test="$errors = 0 and $failures = 0">
                        without a single error or failure!
                    </xsl:when>
                    <xsl:when test="$errors &gt; 0 and $failures = 0">
                        with
                        <xsl:call-template name="plural.singular">
                            <xsl:with-param name="number" select="$errors" />
                            <xsl:with-param name="word" select="'error'" />
                        </xsl:call-template>
                        .
                    </xsl:when>
                    <xsl:when test="$errors = 0 and $failures &gt; 0">
                        with
                        <xsl:call-template name="plural.singular">
                            <xsl:with-param name="number" select="$failures" />
                            <xsl:with-param name="word" select="'failure'" />
                        </xsl:call-template>
                        .
                    </xsl:when>
                    <xsl:otherwise>
                        with
                        <xsl:call-template name="plural.singular">
                            <xsl:with-param name="number" select="$errors" />
                            <xsl:with-param name="word" select="'error'" />
                        </xsl:call-template>

                        and
                        <xsl:call-template name="plural.singular">
                            <xsl:with-param name="number" select="$failures" />
                            <xsl:with-param name="word" select="'failure'" />
                        </xsl:call-template>
                        .
                    </xsl:otherwise>
                </xsl:choose>

            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="plural.singular">
        <xsl:param name="number" />
        <xsl:param name="word" />

        <xsl:choose>
            <xsl:when test="$number = 0">zero <xsl:value-of select="$word" />s</xsl:when>
            <xsl:when test="$number = 1">one <xsl:value-of select="$word" /></xsl:when>
            <xsl:otherwise><xsl:value-of select="$number" /><xsl:text> </xsl:text><xsl:value-of select="$word" />s</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- this is the stylesheet css to use for nearly everything -->
    <xsl:template name="stylesheet.css">
        <![CDATA[
        
        /* html5 boilerplate */
        html, body, div, span, object, iframe,
        h1, h2, h3, h4, h5, h6, p, blockquote, pre,
        abbr, address, cite, code, del, dfn, em, img, ins, kbd, q, samp,
        small, strong, sub, sup, var, b, i, dl, dt, dd, ol, ul, li {
        margin: 0;
        padding: 0;
        border: 0;
        font-size: 100%;
        font: inherit;
        vertical-align: baseline;
        }

        select, input, textarea, button { font:99% sans-serif; }
        pre, code, kbd, samp { font-family: monospace, sans-serif; }

        html { overflow-y: scroll; }
        a:hover, a:active { outline: none; }

        ::-moz-selection{ background: #FF9800; color:#fff; text-shadow: none; }
        ::selection { background: #FF9800; color:#fff; text-shadow: none; }
        a:link { -webkit-tap-highlight-color: #FF9800; }

        h1 { font-size: 2.5em; }
        h1, h2, h3, h4, h5, h6 { font-weight: bold; }
        body, select, input, textarea { color: #333; }

        /* html5 boilerpalte end */

        
        body {
            color: #333333;
            background-color: #F8F8F8;
            font:13px/1.231 ubuntu, sans-serif; *font-size:small;
        }

        p.intro { font-size: 1.5em; }

        a { color: #1A4491; text-decoration: none; }
        a:hover { }

        pre {
            border-radius: 5px;
            margin-bottom: 8px;
            padding: 15px;
            background-color: #FFFFFF;
            border: 1px solid #DEDEDE;
            font-family: Consolas, Monaco, monospace;
            font-size: 0.9em;
            white-space: pre; 
            white-space: pre-wrap; 
            word-wrap: 
            break-word; 
        }
        
        #report {
            border-radius: 8px;
            box-shadow: 0 0 8px #F5F5F5;
    
            background-color: white;
            margin: 10px auto;
            padding: 10px 15px;
        }

        /* Navigation links between the various views
        - - - - - - - - - - - - - - - - - - - - - - */
        #navigationlinks { text-align: right; }
        #navigationlinks p { padding: 2px; }
        #navigationlinks a { font-size: 1.1em; color: #464F38; }
        #navigationlinks a:hover { color: #333; }

        /* Test suites
        - - - - - - */

        .testsuite {
            border-radius: 5px;
            box-shadow: 0 0 4px #F8F8F8;

            background-color: F7F7F7;    
            background: -moz-linear-gradient(center top , #F7F7F7, #FEFEFE);
    
            border: 1px solid #EEEEEE;
            margin: 20px 0;
            text-align: left;
            width: 100%;
        }

        .testsuite .header {
            color: white;
            padding: 5px 7px;
            text-shadow: 0 0 4px rgba(0, 0, 0, 0.2);
            font-size: 1.3em;
    
            border-radius: 5px 5px 0 0;
            box-shadow: 0 0 13px rgba(255, 255, 255, 0.3) inset;
        }

        .testsuite.error .header {
            background-color: #BC2F2F;            
            background: -moz-linear-gradient(#BC2F2F, #C96952);
            background: -webkit-linear-gradient(#BC2F2F, #C96952);
            background: linear-gradient(#BC2F2F, #C96952);
            border-bottom: 1px solid #BE5B5B;
        }

        .testsuite.failure .header {
            background-color: #E69814;
            background: -moz-linear-gradient(#FFB75B, #E69814);
            background: -webkit-linear-gradient(#FFB75B, #E69814);
            background: linear-gradient(#FFB75B, #E69814);
            border-bottom: 1px solid #CD912B;
        }

        .testsuite.success .header {
            background-color: #A6CC3B;
            background: -moz-linear-gradient(#A6CC3B, #CBD53B);
            background: -webkit-linear-gradient(#A6CC3B, #CBD53B);
            background: linear-gradient(#A6CC3B, #CBD53B);
            border-bottom: 1px solid #C4D5B6;
        }

        .testsuite .header h2, h3 { margin: 0; padding: 0; }
        .testsuite .header h3 { font-size: 0.8em; }

        .testsuite .name {
            width: 50%;
        }

        .testsuite .time {
            width: 10%;
        }

        .testsuite .testcase {
            padding: 5px 0;
        }
        

        /* Link to individual test cases
        - - - - - - - - - - - - - - - - - */

        .packagelink {
            border: 1px solid transparent;
            float: left;
            font-size: 1.1em;
            list-style: none outside none;
            padding: 2px 7px 4px 7px;
            margin: 3px;
        }

        .packagelink:hover {
            border-radius: 4px;
            background-color: #f7f7f7;
            border: 1px solid #ddd;
        }

        .packagelink a {
            color: blue;
            text-decoration: none;
            display: inline-block;
        }

        .packagelink.failure a {
            color: #FB6C00 !important;
        }

        .packagelink.error a {
            color: #DD0707 !important;
        }

        .packagelink.success a {
            color: #344804 !important;
        }

        /* force line break for long test names wihtout white-space */
        .message { word-wrap: break-word; }

        .testcase.success .message { color: #595E51; }
        .testcase.error .message { color: #AA0E0E; }
        .testcase.failure .message { color: #FB6C00; }

        .testsuite .testcase:nth-of-type(2n) {
            background-color: #F4F4F4;
            border-bottom: 1px solid #EEEEEE;
            border-top: 1px solid #EEEEEE;
        }

        .testcase .message {
            font-size: 1.1em;
            font-weight: bold;
        }

        .testcase p.summary {
            margin-left: 5px;
            font-size: 1em;
            color: #444;
        }

        .outputinfo p { margin-top: 9px; }

        /* output is parsed using javascript and not visible by default.
        I don't think that having a non-javascript fallback is important
        as most Grails developers won't be using IE 6 :D */

        .testsuite .footer { display: none; }
        p { padding: 4px; }

        .footer.output {
            border-radius: 0 0 5px 5px;
            background-color: #F8F8F8;
            background: -moz-linear-gradient(center top , #F8F8F8, #F2F2F2);
            border-top: 1px solid #EEEEEE;
            margin-top: 10px;
        }

        .footer.output h2 { padding: 5px 0 0 5px; }
        .footer.output .sysout, .syserr { float: left; width: 49%; }
        .footer.output pre { margin: 5px; }

        .errorMessage {
            color: #AA0E0E;
            font-size: 1em;
            font-weight: bold;
        }

        .errorMessage.failure { color: #FB6C00 !important; }

        

        .grailslogo {
            background-image: url("data:image/png;base64,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");
            width: 63px; height: 66px; 
            margin: 0 10px 5px 0;
            float: left;
        }

        /* icons */
        .icon { width: 16px; height: 16px; margin: 6px 4px 0; display: inline-block; }
        .icon.failure { background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAadQTFRF/5YA/5YC/////+3U/+/Y/5gJ/5YE/5YB/9OX/+7X/+rO/5YD/7RP/6w7/7BE/+/Z/8V3/6Yr/9Wd/5cF/5gI/8Ft/5gK/+vO/+bE/8Nv/50Y/6Mi/8Z3//rz/9ef/6gy/+/a/8qC/5gL/+fF/6kz/75l/7FD//r0/9ad/5kL/6Ac/7JL/+bD/6Eg/7th//Tm/6Qm//Pk/50W/+C2/6o3/+O8/6Af/7dU/6Ae/8qD/6Ab/6Ys/7pc/8Jt/6ct//bo//Hd/8Z4/+rN/8V0/5kI/+zR/8h8//v2/7NM/58d/7JM/608/7xh/+TA/7dW/9Sa/+3W/8Ny/6cu/6cv/7BG/7BF/5cH/5YG/5gM/5gH/8Fu/54Z/9ae/9af//Tl/58e//Db/8h9/7xi/6gw/7xf/7pb/8Br/7RQ/8Rx/7BD/6w8/96x//Dd/5sR/5cG//Ph/6Yu//Lg/8Ju/8uF/7FG/8Bs/8Fs/5kN/82K/+rP/+bC/6k2/5cJ/+XA/50X/8qE/86J/5wO/9ur/8qB/+S//8yF/8uC/9CS//ft/5cI/5sM//ju////XEMRUwAAAI10Uk5T//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////8A2F5VawAAAPlJREFUeNosj2Vvw0AAQ30XTsq4csfMzMzMjO2YmZm3+9FL01myZL1PfmCJKLuLjpcRY0Jv9OqzfGFYPq1VkiDlxOaeuzh3jwYzBxNgb2jTbO6fmLT07Zh6rDrYiFQVqSa/P19dWx4fYOB7hfps2lwRWaLdR64yDo4ZbfaV0uJSSt87tG8Zq4JI0P4VCv0GQcTpVnQJEoG98/CYi0Ejog05HglN9xzPcwUZkJwy+H0EKtNVX6P35voWuRzYuvDz0bYF+LxPz85q/UdLYOpgmxAiZbnEuqhxvSQNnof5S9wVviXlVuJ59jPLWE2D9d+WMT78mBpWjPknwABNFkK/nayT6wAAAABJRU5ErkJggg=="); }
        .icon.error { background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAk9QTFRF////ykY0/5GK/5KK+vb1+eLg+lNG2UYz8OPg/3xz/G1j6riz9drY942E7peN6UYz0UY28kY02EY02kY17kY200c2/1RF/1VHzEY0/5uS9Uk69Onn/5aOz0Y01kg39Ojm/3Rq/nVr30s53Es76Eo6+fLw+21i9Hhw/7ex/GRY67Ks74qC9ray6kYz/v//7rKs30Yz4ltN/GZb+uPh7dPP7WNY9+Lg7VNF7dHM8c/L9FdM90Yz+WRY8VdK9+bk9ldL/nRp32JW6mJW3Ec48tTQ70k68Hxy8t3a75WM8KKb+eHf/Eo7/Pn47NzY3kY170k5+Ovq+WJW7aSb4U89+uDe/Pf2+pOL/3xy3Ew88JGI+Ozr3F1O/a2o31BB10Yz+ubk6Z+X8Hhv6s3J1kY25kg39+Ph1kY17FdK6Uk699PQ/5KL8EY09qih/W5j4oh/3EY079nZ75uS9bWx4IF270Y04l9R79/c1Uo69sTB7mpf4WZZ6JuT77aw/7Ks/nNp7Lex+fPx7lhL9uLg20k64lA+7+Dc+IF44ks67pWM9EY09+Ti5Whe2GVX5Eg67qmg6NTO8m1i/KSe8aWd0kYz9sC86tzY+ezr7kk6/5qS9NrY3UYz7pqQ4YZ89kYz7omB/7Su5p6W7YqB9uPh69vY6Ug4/Uw96NXR/8nE56Oc8HVp22hc1kYz+e7tzkYz4FxO+uHf56Ob73Rp7mZb5ZqS50s94FdJ83pw9tDM6dvX+N/d9uHf/3Rr9Us867izxEY044d99KOc7pSK9t/d8uDc////NUopuQAAAMV0Uk5T/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////wA0VseRAAABBElEQVR42mI4AgLrK3SKpoSAmQxAbJwTs1xjfu3EA14QgcywGcxZTEzMMpK820EC8W2cahNydyvUqXOFb2MFCkQaWc2b6cbGlsijba+58AgDq+10p1CGNZWbohh8TF3NdzLUbBFW8mRgmBbAoDep3a9XnyGhWUBo1mxVFpaGQoP8ZOdqhgXl+2RXO7pLyx+uYk+TEN3MUK/MuLbHopWDo8xk1WRGOS0Gs1j26KRlizsO+vYXdMdFHGI4MkclOGgHH7+ux9S+lpRioDvs1vkv2Vgi2Jm9MkNkayrI6S6le+Z6i4kvtZYytIR4risvPVDRwaZx/yKob48caeJesWHvLjATIMAAiZBvi5wj6TUAAAAASUVORK5CYII="); }
        .icon.success { background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAn9QTFRF////BcEAWfsgGNYCA4YAINsDZPopA4QABaAAC8QAPOsOkP9TBcMABcYABccACMgAEdEAB4YADcwABMEADrcADcwAENAABsgABI8AAIMAAYoAC5gBAYwABJQABYwAApEACJoAEKwBGbMEEK4BDa0BEK0BEbMBFbcCA4gAA4oAGKsEA5MAB50ACqUBDJABD6kBG7EEPL4TLr4LHrYFKb4JM9wMJcoFJdQFN8AQSeMZWNUjDcgAEMIBF8sCG9cCJNMFK+IHMN4KNMQOPOoOPukQQ8EWSPIXS/IXTMgcTNQbT8geUPQaUeocUt8eV/cgWfogXOYmYf4mZuwtav8ta+YybvI0c/Y5d/c6d/g7hPFKivFRk+5clPFemO5jmvRln/BtpOtyreyBrfB/sOqEse2CtOqJWKAqWaErWqEsW6ItXKMuXaQuXqUvX6UwYKYxYacyYqgzY6g0ZKk1Zao2Zqs2Z6s3aKw4aa05aq46a647bK88bbA9brE+b7E/cLJAcbNBcrNCc7RDdLVEdrZFd7ZGeLdHebhIerhJe7lKfLpLfbpNfrtOf7xPgLxQgb1Rgr5Sg79ThL9UhcBVhsFXh8FYicJZisNai8NbjMRcjcRejsVfj8ZgkMZhkcdjkshkk8hllMlmlcpolsppmMtqmcxrmsxtm81unM1vnc5xns9yn89zoNB0odF2otF3pNJ5pdJ6ptN7p9R9qNR+qdV/qtWBq9aCrNeErdeFrtiGsNiIsdmJstqLs9qMtNuOtduPttyRt92SuN2Uut6Vu96XvN+YveCavuCbv+GdwOGeweKgwuKhxOOjxeSlxuSmx+WoyOWpyearyuaty+euzeiwzuiyC5FmAAAAAAF0Uk5TAEDm2GYAAACFSURBVBgZfcGhCsMwEAbg/1FPRMREVETUVRwcFCIiyk3EJRAxs2eIDaNmD7SOMtqK7fuAfwRXMnmcCb+Sw0H4qaoWQAjYzNOaggwWkBBnIHDPqs4CHNce4sK9JnUWwJCpNZFGJXmPD7cUYxrdKwt2/kabyoovl4hIMg5jeTDhbBSDK8JPb8CGNtTmyz9LAAAAAElFTkSuQmCC"); }
        
        /* 960gs */
        body{min-width:1200px}
		.container_8{margin-left:auto;margin-right:auto;width:1200px}
		.grid_1,.grid_2,.grid_3,.grid_4,.grid_5,.grid_6,.grid_7,.grid_8{display:inline;float:left;position:relative;margin-left:15px;margin-right:15px}
		.push_1,.pull_1,.push_2,.pull_2,.push_3,.pull_3,.push_4,.pull_4,.push_5,.pull_5,.push_6,.pull_6,.push_7,.pull_7,.push_8,.pull_8{position:relative}
		.alpha{margin-left:0}
		.omega{margin-right:0}
		.container_8 .grid_1{width:120px}
		.container_8 .grid_2{width:270px}
		.container_8 .grid_3{width:420px}
		.container_8 .grid_4{width:570px}
		.container_8 .grid_5{width:720px}
		.container_8 .grid_6{width:870px}
		.container_8 .grid_7{width:1020px}
		.container_8 .grid_8{width:1170px}
		.container_8 .prefix_1{padding-left:150px}
		.container_8 .prefix_2{padding-left:300px}
		.container_8 .prefix_3{padding-left:450px}
		.container_8 .prefix_4{padding-left:600px}
		.container_8 .prefix_5{padding-left:750px}
		.container_8 .prefix_6{padding-left:900px}
		.container_8 .prefix_7{padding-left:1050px}
		.container_8 .suffix_1{padding-right:150px}
		.container_8 .suffix_2{padding-right:300px}
		.container_8 .suffix_3{padding-right:450px}
		.container_8 .suffix_4{padding-right:600px}
		.container_8 .suffix_5{padding-right:750px}
		.container_8 .suffix_6{padding-right:900px}
		.container_8 .suffix_7{padding-right:1050px}
		.container_8 .push_1{left:150px}
		.container_8 .push_2{left:300px}
		.container_8 .push_3{left:450px}
		.container_8 .push_4{left:600px}
		.container_8 .push_5{left:750px}
		.container_8 .push_6{left:900px}
		.container_8 .push_7{left:1050px}
		.container_8 .pull_1{left:-150px}
		.container_8 .pull_2{left:-300px}
		.container_8 .pull_3{left:-450px}
		.container_8 .pull_4{left:-600px}
		.container_8 .pull_5{left:-750px}
		.container_8 .pull_6{left:-900px}
		.container_8 .pull_7{left:-1050px}
		.clear{clear:both;display:block;overflow:hidden;visibility:hidden;width:0;height:0}
		.clearfix:before,.clearfix:after{content:'\0020';display:block;overflow:hidden;visibility:hidden;width:0;height:0}
		.clearfix:after{clear:both}
		.clearfix{zoom:1}

        ]]>
    </xsl:template>
    

    <!-- transform string like a.b.c to ../../../ @param path the path to transform into a descending directory path -->
    <xsl:template name="path">
        <xsl:param name="path" />
        <xsl:if test="contains($path,'.')">
            <xsl:text>../</xsl:text>
            <xsl:call-template name="path">
                <xsl:with-param name="path">
                    <xsl:value-of select="substring-after($path,'.')" />
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="not(contains($path,'.')) and not($path = '')">
            <xsl:text>../</xsl:text>
        </xsl:if>
    </xsl:template>


    <!-- create the link to the stylesheet based on the package name -->
    <xsl:template name="create.resource.links">
        <xsl:param name="package.name" />
        <link rel="shortcut icon" href="http://grails.org/images/favicon.ico" type="image/x-icon"></link>
        <link rel="stylesheet" type="text/css" title="Style">
            <xsl:attribute name="href"><xsl:if test="not($package.name = 'unnamed package')"><xsl:call-template name="path"><xsl:with-param name="path" select="$package.name" /></xsl:call-template></xsl:if>stylesheet.css</xsl:attribute>
        </link>
    </xsl:template>

    <!-- create the link to the home page wrapped around the grails logo -->
    <xsl:template name="create.logo.link">
        <xsl:param name="package.name" />
        <a title="Home">
            <xsl:attribute name="href"><xsl:if test="not($package.name = 'unnamed package')"><xsl:call-template name="path"><xsl:with-param name="path" select="$package.name" /></xsl:call-template></xsl:if>index.html</xsl:attribute>
            <div class="grailslogo"></div>
        </a>
    </xsl:template>

    <!-- create the links for the various views -->
    <xsl:template name="navigation.links">
        <xsl:param name="package.name" />
        <nav id="navigationlinks">
            <p>
                <a>
                    <xsl:attribute name="href"><xsl:if test="not($package.name = 'unnamed package')"><xsl:call-template name="path"><xsl:with-param name="path" select="$package.name" /></xsl:call-template></xsl:if>failed.html</xsl:attribute>
                    Tests with failure and errors
                </a>
            </p>
            <p>
                <a>
                    <xsl:attribute name="href"><xsl:if test="not($package.name = 'unnamed package')"><xsl:call-template name="path"><xsl:with-param name="path" select="$package.name" /></xsl:call-template></xsl:if>index.html</xsl:attribute>
                    Package summary
                </a>
            </p>
            <p>
                <a>
                    <xsl:attribute name="href"><xsl:if test="not($package.name = 'unnamed package')"><xsl:call-template name="path"><xsl:with-param name="path" select="$package.name" /></xsl:call-template></xsl:if>all.html</xsl:attribute>
                    Show all tests
                </a>
            </p>
        </nav>
    </xsl:template>

    <!-- template that will convert a carriage return into a br tag @param word the text from which to convert CR to BR tag -->
    <xsl:template name="br-replace">
        <xsl:param name="word" />
        <xsl:value-of disable-output-escaping="yes" select='stringutils:replace(string($word),"&#xA;","&lt;br/>")' />
    </xsl:template>

    <xsl:template name="display-time">
        <xsl:param name="value" />
        <xsl:value-of select="format-number($value,'0.000')" />
    </xsl:template>

    <xsl:template name="display-percent">
        <xsl:param name="value" />
        <xsl:value-of select="format-number($value,'0.00%')" />
    </xsl:template>


    <xsl:template name="output.parser.js">
        <xsl:comment>
            Parses JUnit output and associates it with the corresponding test case
        </xsl:comment>
        <script language="javascript">
<![CDATA[

/**
 * The JUnit report format is incredibly stuipd in the
 * sense that it accumulates output from all test methods
 * into a single xml node.
 */
(function() {

    var outputElements = findOutputElements();
    for (var i in outputElements) {
        var outputElement = outputElements[i];
        var textOutput = outputElement.element.firstChild.nodeValue;
        var header = outputElement.getHeader();
        appendTestMethodOutput(textOutput, header);
    }
    
    function findOutputElements() {
        var outputElements = [];
        var preElements = document.getElementsByTagName("pre");
        for (var i in preElements) {
            var preElement = preElements[i];
            var className = preElement.className || "";
            if (className.indexOf("stdout") >= 0) {
                var outputElement = new OutputElement(preElement, "output");
                outputElements.push(outputElement);
            } else if (className.indexOf("syserr") >= 0) {
                var outputElement = new OutputElement(preElement, "error");
                outputElements.push(outputElement);            
            }
        }
        
        return outputElements;
    }
    
    function OutputElement(element, type) {
        this.element = element;
        this.type = type;
        
        this.getHeader = function() {
            if (type === "output") {
                return "System output";
            } else if ("error") {
                return "System error";
            }
        }
    }
    
    
    function appendTestMethodOutput(text, header) {
        var testOutput = new TestMethodOutput(header);
        
        var lines = text.split(/\r\n|\r|\n/);        
        for (var i in lines) {
            var line = lines[i];
            var matches = line.match(/^--Output from (.*)--$/);
            if (matches !== null && matches.length == 2) { 
                testOutput.flushToDom();
                testOutput.testName = matches[1];
            } else {
                testOutput.addLine(line);
            }
        }
        
        testOutput.flushToDom();
    }
    
    function TestMethodOutput(header) {
        this.header = header;
        this.testName = undefined;
        this.buffer = "";
        
        this.addLine = function(line) {
            this.buffer += line + "\n";
        }
        
        this.flushToDom = function() {
            if (this.testName !== undefined) {
                var domNode = getTestcaseElementByName(this.testName);
                if (domNode !== undefined && trimString(this.buffer).length > 0) {
                    this.appendTo(domNode);
                }
                
                this.reset();
            }
        }
        
        this.appendTo = function(domNode) {
            var node = document.createElement("div");
            node.innerHTML = '<p><b class="message">' + header + '</b></p>';
            
            var preNode = document.createElement("pre"); 
            preNode.appendChild(document.createTextNode(this.buffer)); 
            node.appendChild(preNode);
            
            var outputContainer = findElementByTagClassAndParent("div", "outputinfo", domNode);
            outputContainer.appendChild(node); 
        }
        
        this.reset = function() {
            this.methodName = undefined;
            this.buffer = "";
        }
    }
    
    function getTestcaseElementByName(name) {
        var divElements = document.getElementsByTagName("div");
        var elementCount = divElements.length;
        for (var i=0; i<elementCount; i++) {
            var el = divElements[i];
            if (el.getAttribute("data-name") === name) {
                return el;
            }
        }
    }
    
    function findElementByTagClassAndParent(tagName, className, parentNode) {
        var elements = parentNode.getElementsByTagName(tagName); 
        for (var i in elements) {
            var element = elements[i];
            
            // Not 100% correct, but good enough here
            if (element.className !== undefined && element.className.indexOf(className) >= 0) {
                return element;
            }
        }
    }
    
    function trimString(str) {
        return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
    }
    
})();
]]>
        </script>
    </xsl:template>


</xsl:stylesheet>
