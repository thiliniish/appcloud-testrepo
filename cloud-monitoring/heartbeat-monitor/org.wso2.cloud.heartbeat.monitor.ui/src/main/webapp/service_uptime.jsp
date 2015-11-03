<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.CloudStructure" %>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.utils.ConfigReader" %>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.utils.StringConverter" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>

<head>
    <%
        //allow access only if session exists
        String user = (String) session.getAttribute("user");
        String userName = null;
        String sessionID = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("user"))
                    userName = (String) request.getSession(false).getAttribute("user");
                if (cookie.getName().equals("JSESSIONID"))
                    sessionID = cookie.getValue();
            }
        }
    %>
</head>
<script type="text/javascript">


    $(document).on('keyup', function (e) {
        if (e.keyCode == 27) $('#fade').click()
    });


    /* Get Time zone from browser and add it to time  */

    $.fn.addTimeZoneDiff = function (time) {
        var offset = new Date().getTimezoneOffset() * 60 * 100;
        return time + offset;
    };

    /* traverse the json object to populate server list */

    $.fn.populateServers = function (json) {
        $.each(json, function (index, value) {
            $("#Server").append($("<option></option>")
                    .attr("value", value)
                    .text(value.replace(/([A-Z])/g, ' $1').trim()));
        });
    };

    /* traverse the json object to populate test list */

    $.fn.populateTests = function (json) {
        $.each(json, function (index, value) {
            $("#test-name").append($("<option></option>")
                    .attr("value", value)
                    .text(value.replace(/([A-Z])/g, ' $1').trim()));
        });
    };

    /* traverse the json object to populate test list make a sortable array */
    $.fn.uptimeInfo = function (json) {
        $.each(json.UptimeInfo, function (index, value) {
            var array = Array();
            var t1 = new Date(index).getTime();
            array.push([t1, value]);
            return array;
        });
    };

    /* Uptime information graph populated using the json values parsed */
    $.fn.populateUptime = function (json, array) {
        $.each(json.UptimeInfo, function (index, value) {
            var t1 = new Date(index).getTime();
            t1 = $.fn.addTimeZoneDiff(t1);
            array.push([t1, value]);
        });

        $.each(json.FailureDetails, function (index, value) {
            var replaced = index.replace(/[{}]/g, "");
            var splitted = replaced.split('=');
            var serverName = splitted[0];
            var testNameU = splitted[1];
            var testSeverity = splitted[2];

            var indexn;
            for (indexn = 0; indexn < value.length; indexn++) {
                var startTime = value[indexn]["left"];
                var endTime = value[indexn]["right"];
                var duration = Math.round((endTime - startTime) / 1000);

                $("#uptime-info").append($("<tr id=" + startTime + "><td>" + new Date(startTime) + "</td><td>" + new Date(endTime) + "</td><td>" + duration + " s</td><td>" + serverName + "</td><td>" + testNameU + "</td><td>" + testSeverity + "</td><td><button class='changeStatus' value='" + startTime + "/" + endTime + "/" + serverName + "/" + testNameU + "'></button></td></tr>"));
            }
        });
        $.fn.plotFunction(array, json.successRate, json.failureCount, json.downTime);
    };

    /* Controlled pagination and row sorting for uptime info table*/
    $.fn.completePagination = function () {
        $('#load').css({'display': "none"});
        var $tbody = $('#uptime-info');
        $tbody.find('tr').sort(function (a, b) {
            var tda = $(a).attr('id');
            var tdb = $(b).attr('id');
            // if a > b return 1
            return tda < tdb ? 1
                // else if a < b return -1
                    : tda > tdb ? -1
                // else they are equal - return 0
                    : 0;
        }).appendTo($tbody);

        $('#navig').empty();
        $('#uptime-info').after('<div id="navig"></div>');
        var rowsShown = 25;
        var rowsTotal = $('#uptime-info tr').length;
        var numPages = rowsTotal / rowsShown;
        for (i = 0; i < numPages; i++) {
            var pageNum = i + 1;
            $('#navig').append('<a href="#uptime-info" rel="' + i + '">' + pageNum + '</a> ');
        }
        $('#uptime-info tr').hide();
        $('#uptime-info tr').slice(0, rowsShown).show();
        $('#navig a:first').addClass('active');
        $('#navig a').bind('click', function () {
            $('#navig a').removeClass('active');
            $(this).addClass('active');
            var currPage = $(this).attr('rel');
            var startItem = currPage * rowsShown;
            var endItem = startItem + rowsShown;
            $('#uptime-info tr').css('opacity', '0.0').hide().slice(startItem, endItem).
                    css('display', 'table-row').animate({opacity: 1}, 300);
        });
    };

    /* Updates the service uptime graphs and all main function */
    $.fn.serviceUptimeUpdateFunction = function () {
        var cloudName = $("#Cloud option:selected").val();
        var serverName = $("#Server option:selected").val();
        var testName = $("#test-name option:selected").val();
        var severity = $("#severity option:selected").val();
        var dateSelectorTime = jQuery.parseJSON(JSON.stringify($("#datePicker").daterangepicker("getRange")));
        var array = Array();
        var startTime;
        var endTime;

        $("#uptime-info").empty();
        $("#successRate").empty();
        $("#downTime").empty();
        $("#failureCount").empty();
        $('#navig').empty();
        $.plot("#placeholder", []);
        startTime = dateSelectorTime.start;
        endTime = dateSelectorTime.end;
        var startDate = moment(startTime, "YYYY-MM-DD'T'HH:mm:ss'Z'").add('hours', 0).format('YYYY-MM-DD HH:mm:ss');
        var endDate = moment(endTime, "YYYY-MM-DD'T'HH:mm:ss'Z'").add('hours', 24).format('YYYY-MM-DD HH:mm:ss');


        $('#load').css({'display': "block"});
        if (typeof serverName === 'undefined' || serverName == "All") {
            $.ajax({
                type: 'POST',
                url: 'update_serverList.jsp',
                data: {
                    cloudName: cloudName, serverName: "All", severity: severity
                },
                dataType: "JSON",
                success: function (json) {
                    $("#Server").empty();
                    $("#test-name").empty();
                    $.fn.populateServers(json.Services);
                }
            });

            $("#uptime-info").empty();
            $("#uptime-info").append($("<th>Start Time</th> <th>End Time</th> <th>Duration (s)</th><th>Server Name</th> <th>Test Name</th><th>Severity</th><th></th>"));
            $.ajax({
                type: 'POST',
                url: 'update_servers.jsp',
                data: {
                    cloudName: cloudName,
                    timeIntervalStart: startDate,
                    timeIntervalEnd: endDate,
                    severity: severity
                },
                dataType: "JSON",
                success: function (json) {
                    $.fn.populateUptime(json, array);
                }, complete: function () {
                    $.fn.completePagination();
                }
            });
        }
        else if (typeof testName === 'undefined') {
            testName = "All";

            $.ajax({
                type: 'POST',
                url: 'update_serverList.jsp',
                data: {
                    cloudName: cloudName, serverName: serverName, severity: severity
                },
                dataType: "JSON",
                success: function (json) {
                    $("#Server").empty();
                    $.fn.populateServers(json.Services);
                }
            });
            $.fn.updateGraph(serverName, testName, startDate, endDate);
        }
        else {
            $.ajax({
                type: 'POST',
                url: 'update_serverList.jsp',
                data: {
                    cloudName: cloudName, serverName: serverName, severity: severity
                },
                dataType: "JSON",
                success: function (json) {
                    $("#Server").empty();
                    $.fn.populateServers(json.Services);
                }
            });

            $.fn.updateGraph(serverName, testName, startDate, endDate);
        }
    };

    /* updates graph */

    $.fn.updateGraph = function (serverName, testName, startDate, endDate) {
        var array = Array();
        var severity = $("#severity option:selected").val();

        $("#uptime-info").empty();
        $("#uptime-info").append($("<th>Start Time</th> <th>End Time</th><th>Duration (s)</th> <th>Server Name</th> <th>Test Name</th><th>Severity</th><th></th>"));
        $.ajax({
            type: 'POST',
            url: 'update_graph.jsp',
            data: {
                serverName: serverName,
                testName: testName,
                timeIntervalStart: startDate,
                timeIntervalEnd: endDate,
                severity: severity
            },
            dataType: "JSON",
            success: function (json) {
                if (json.UptimeInfo == "No Records") {
                    $.plot("#placeholder", [array.sort()], {
                        xaxis: {mode: "time"}
                    });
                    $("#successRate").text("No records for" + serverName + " and " + testName);
                    $("#failureCount").text("No outages");
                    $("#downTime").text("No down time");
                }
                else {
                    $("#test-name").empty();
                    $.fn.populateUptime(json, array);
                    $.fn.populateTests(json.Tests);
                }
            },
            complete: function () {
                $.fn.completePagination();
            }
        });
    };


    /* Jquery flot build up related data */
    $.fn.plotFunction = function (array, successRate, failureCount, downTime) {
        var options = {
            xaxis: {mode: "time"},
            yaxis: {
                min: 0, max: 1.25, tickSize: 0.5
            },
            series: {
                lines: {
                    show: true,
                    fill: true,
                    fillColor: {colors: [{opacity: 0.7}, {opacity: 0.1}]}
                },
                points: {
                    show: true,
                    radius: 2
                }
            },
            grid: {
                hoverable: false,
                clickable: false
            },
            colors: ["#01DF3A"]
        };


        $.plot("#placeholder", [array.sort()], options);

        $("#successRate").text(successRate + "%");
        $("#failureCount").text("(" + failureCount + " outages)");
        var num = downTime;
        var seconds = Math.floor(num / 1000);
        var minutes = Math.floor(seconds / 60);
        var seconds = seconds - (minutes * 60);
        $("#downTime").text(minutes + ' m ' + seconds + ' s');
    }
</script>

<script>
    $('.changeStatus').live('click', function () {
        var selectedRow = $(this).val();
        var parameterArray = selectedRow.split("/");
        var startTime = parameterArray[0];
        var endTime = parameterArray[1];
        var serverName = parameterArray[2];
        var testName = parameterArray[3];

        var startDate = new Date(parseInt(startTime));
        var endDate = new Date(parseInt(endTime));

        $("#nullAlert").empty();
        $("#nullAlertAlarm").empty();
        $("#failure-info").empty();
        $("#failure-info").append($("<th>Incident Time</th> <th>Incident Detail</th> <th>Jira Link</th> <th>Row Select</th><th></th>"));
        $("#incidentInformation").text("Failure Detail Information from " + startDate.toLocaleString() + " to " + endDate.toLocaleString());
        $.ajax({
            type: 'POST',
            url: 'failure_detail.jsp',
            data: {
                serverName: serverName, testName: testName, timeIntervalStart: startTime,
                timeIntervalEnd: endTime
            },
            dataType: "JSON",
            success: function (json) {
                $.each(json.failureInfo, function (index, value) {
                    $("#failure-info").append($("<tr id=" + value[1] + "><td>" + index.toLocaleString() + "</td><td>" + value[0] + "</td><td>" + value[2] + "</td><td><input id=" + value[1] + " type='checkbox' /></td></tr>"));
                });
            }, complete: function () {
                var $tbody = $('#failure-info');
                $tbody.find('tr').sort(function (a, b) {
                    var tda = $(a).attr('id');
                    var tdb = $(b).attr('id');
                    // if a > b return 1
                    return tda < tdb ? 1
                        // else if a < b return -1
                            : tda > tdb ? -1
                        // else they are equal - return 0
                            : 0;
                }).appendTo($tbody);
            }
        });
        $("#popupServerName").text(serverName);
        $("#popupTestName").text(testName);

        $("#light").css({'display': "block"});
        $("#fade").css({'display': "block"});
    });

    $('#cancelReason').live('click', function () {
        $("#falseAlarmReason").val("");


    });

    $('#cancelJira').live('click', function () {
        $("#jiraLink").val("");
    });


    $('#fade').live('click', function () {
        $("#jiraLink").val("");
        $("#falseAlarmReason").val("");
        $("#nullAlert").val("");
        $("#nullAlertAlarm").val("");
        $("#light").css({'display': "none"});
        $("#fade").css({'display': "none"});
    });


    $('#submitFalseAlarm').live('click', function () {
        var alarmReasonEntered = $("#falseAlarmReason").val();
        if (alarmReasonEntered == '') {
            $("#nullAlertAlarm").css({'display': "block"});
            $("#nullAlertAlarm").text("Please fill the required alarm reason field");
        }
        else {
            $("#nullAlert").css({'display': "block"});
            var checkboxes = $("input:checked");
            var idArray = [];
            for (var i = 0; i < checkboxes.length; i++) {
                var checkbox = checkboxes[i];
                var currentRow = checkbox.parentNode.parentNode;
                idArray.push(checkbox.id);
            }
            $.ajax({
                type: 'POST',
                url: 'set_true.jsp',
                data: {
                    failureIndexes: idArray.toString(),
                    changeInfo: alarmReasonEntered
                },
                dataType: "JSON",
                success: function (json) {
                    $("#light").css({'display': "none"});
                    $("#fade").css({'display': "none"});
                    console.log(json.Status);
                    $.fn.serviceUptimeUpdateFunction();
                }
            });
        }
    });

    $('#submitJira').live('click', function () {

        var jiraUrlEntered = $("#jiraLink").val();
        if (jiraUrlEntered == '') {
            $("#nullAlert").css({'display': "block"});
            $("#nullAlert").text("Please fill the required Jira URL field");
        }
        else {
            $("#nullAlert").css({'display': "block"});
            var checkboxes = $("input:checked");
            var idArray = [];
            for (var i = 0; i < checkboxes.length; i++) {
                var checkbox = checkboxes[i];
                var currentRow = checkbox.parentNode.parentNode;
                idArray.push(checkbox.id);
            }
            $.ajax({
                type: 'POST',
                url: 'set_jira.jsp',
                data: {
                    jiraUrl: jiraUrlEntered, checkedIds: idArray.toString()
                },
                dataType: "JSON",
                success: function (json) {
                    console.log(json.Status);
                    $.fn.serviceUptimeUpdateFunction();
                }
            });
        }
    });
</script>


<div class="main" id="mainTable">
    <div class="service-uptime">

        <div class="select">
            <h4>Cloud</h4>
            <select class="selectvrs" id="Cloud">
                <%
                    ConfigReader configurationInstance = ConfigReader.getInstance();
                    Map<String, CloudStructure> retrievedCloud =
                            configurationInstance.getCloudStructure();
                    Set<String> cloudList = retrievedCloud.keySet();
                    for (String cloudListIterate : cloudList) {
                %>
                <option value="<%=cloudListIterate%>"><%=StringConverter
                        .splitCamelCase(cloudListIterate)%>
                </option>

                <%}%>

            </select>

            <script>
                $(function () {
                    $("#datePicker").daterangepicker();
                    var today = moment().subtract(0, 'days').startOf('day').toDate();
                    var yesterday = moment().subtract(<%=configurationInstance.getTimeInterval()%>, 'days').startOf('day').toDate();
                    $("#datePicker").daterangepicker("setRange", {start: yesterday, end: today});
                    $("#datePicker").change(function () {
                        $.fn.serviceUptimeUpdateFunction();
                    });
                });
            </script>

            <script>
                $("#Cloud").change(function () {
                    $("#Server").val('All');
                    $.fn.serviceUptimeUpdateFunction();
                }).change();
            </script>

            <h4>Server Name</h4>
            <select class="selectvrs" id="Server">
            </select>

            <script>
                $("#Server")
                        .change(function () {
                            $.fn.serviceUptimeUpdateFunction();
                        });
            </script>

            <h4>Test Name</h4>
            <select class="selectvrs" id="test-name">
            </select>
            <script>
                $("#test-name")
                        .change(function () {
                            $.fn.serviceUptimeUpdateFunction();
                        });
            </script>


            <h4>Time Interval</h4>

            <input type="button" style="float:left; width:10px; height:10px; margin-top:10px;"
                   id="datePicker" name="from">

        </div>
        <div class="demo-container">
            <div id="placeholder" class="demo-placeholder"></div>
        </div>
        <div class="uptimedetails">
            <div class="severity">
                <h4>Severity</h4>
                <select class="contain-details" id="severity">
                    <option value="2">2</option>
                    <option value="1">1</option>
                    <option value="0">0</option>
                </select>
            </div>

            <script>
                $("#severity")
                        .change(function () {
                            $.fn.serviceUptimeUpdateFunction();
                        });
            </script>

            <div class="contain-details">
                <h4>UPTIME</h4>

                <h3 id="successRate"></h3>
            </div>
            <div class="contain-details">
                <h4>DOWNTIME</h4>

                <h3 id="downTime"></h3>

                <p id="failureCount"></p>
            </div>
        </div>

        <div class="uptime_info_div">
            <table id="uptime-info">
            </table>
        </div>

        <div id="light" class="white_content">
            <h1 id="incidentInformation"></h1>

            <h1>Server Name</h1>

            <h2 id="popupServerName"></h2>

            <h1>Test Name</h1>

            <h2 id="popupTestName"></h2>

            <h5 id="nullAlert">
            </h5>

            <h1>Jira Link</h1><input id="jiraLink" type="text"/>
            <input id="submitJira" type="button" value="Set link"/>
            <input id="cancelJira" type="button" value="Cancel"/>


            <h5 id="nullAlertAlarm">
            </h5>

            <h1>False Alarm Reason</h1><input id="falseAlarmReason" type="text"/>
            <input id="submitFalseAlarm" type="button" value="Submit"/>
            <input id="cancelReason" type="button" value="Reset"/>

            <table id="failure-info">
            </table>
        </div>
        <div id="fade" class="black_overlay"></div>

        <div id="load" class="loading-div">
            <img id="loadingimage" src="images/loading.gif"
                 style="margin-top:25%; margin-left:45%;"/>
        </div>
    </div>
</div>
</div>

