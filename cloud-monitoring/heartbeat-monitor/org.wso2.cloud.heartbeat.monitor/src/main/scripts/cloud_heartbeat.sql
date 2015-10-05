-- --------------------------------------------------------
--
-- Create Database: `cloud_heartbeat`
--
CREATE DATABASE cloud_heartbeat;
USE cloud_heartbeat;
-- --------------------------------------------------------

--
-- Table structure for table `FAILURE_DETAIL`
--

CREATE TABLE IF NOT EXISTS `FAILURE_DETAIL` (
  `FAILUREINDEX` int(11) NOT NULL AUTO_INCREMENT,
  `TIMESTAMP` bigint(20) NOT NULL,
  `SERVICE` varchar(50)  NOT NULL,
  `TEST` varchar(50)  NOT NULL,
  `DETAIL` text,
  `JIRALINK` VARCHAR(100) DEFAULT NULL,
  `DATETIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ALARMSTATUS` tinyint(4) NOT NULL DEFAULT '1',
  PRIMARY KEY (`TIMESTAMP`,`SERVICE`,`TEST`),
  UNIQUE KEY `FAILUREINDEX` (`FAILUREINDEX`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `LIVE_STATUS`
--

CREATE TABLE IF NOT EXISTS `LIVE_STATUS` (
  `TIMESTAMP` bigint(20) NOT NULL,
  `SERVICE` varchar(50)  NOT NULL,
  `TEST` varchar(50)  NOT NULL,
  `STATUS` tinyint(1) NOT NULL,
  `DATETIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `SEVERITY` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`TIMESTAMP`,`SERVICE`,`TEST`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `HISTORY_NOTES`
--

CREATE TABLE IF NOT EXISTS `HISTORY_NOTES` (
  `SERVICE` varchar(50) NOT NULL,
  `DATETIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `NOTE` text NOT NULL,
  PRIMARY KEY (`SERVICE`,`DATETIME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `HISTORY`
-- summarized tests' data
--

CREATE TABLE IF NOT EXISTS `HISTORY` (
  `SERVICE` varchar(50) NOT NULL,
  `DATE` date NOT NULL,
  `STATUS` enum('NORMAL','DISRUPTION','DOWN','NA') NOT NULL,
  KEY `SERVICE` (`SERVICE`,`DATE`),
  KEY `DATE` (`DATE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `FALSE_FAILURES`
--

CREATE TABLE IF NOT EXISTS `FALSE_FAILURES` (
  `FAILUREINDEX` int(11) NOT NULL,
  `TIMESTAMP` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `USERID` varchar(50) NOT NULL,
  `CHANGEINFO` varchar(500) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

