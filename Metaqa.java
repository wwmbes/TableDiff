/**
   ################################################################
   # metaqa: A program for comparing any source data
   #         with any target data by using SQL.
   # Copyright (C) 2006, 2007, 2008  Woolworths (PTY) Ltd. South Africa.
   # Contact obscured Email address: wwmbes at Woolworths dot co dot za
   #
   # This program is free software; you can redistribute it and/or
   # modify it under the terms of the GNU General Public License
   # as published by the Free Software Foundation; either version 2
   # of the License, or (at your option) any later version.
   #
   # This program is distributed in the hope that it will be useful,
   # but WITHOUT ANY WARRANTY; without even the implied warranty of
   # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   # GNU General Public License for more details.
   #
   # You should have received a copy of the GNU General Public License
   # along with this program; if not, write to the Free Software
   # Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
   #
   # Refer to gpl.txt distributed with this file.
   ################################################################

 * ###############################################################
 * Purpose: Audit any target data, based on another source of data
 *          using SQL.
 * Input  : A data <source>, a <target> table and command-line <parameters>.
 * Output : Primarily, a <spreadsheet> of differences, a target query <file>
 *          and standard screen <status> text output.
 * Scenarios: This program caters for the following audit scenarios:
 *      1) Compare SQL to TEMP table.
 *      2) Compare SQL to file.
 *      3) Compare SQL to table.
 *      4) Compare file to TEMP table.
 *      5) Compare file to file.
 *      6) Compare file to table.
 *  Author:  Mark Besaans. August 2006.
 * ###############################################################
 * History: This program was orgiginally concieved by meta-data analysts
 *      and implemented using Excel. This approach was obviously limited
 *      in many ways, like volumes of 20000 rows and performance of hours.
 *      The first dedicated program to do this task was written in Informix 4GL
 *      in two days flat in 2006 and grew in scope and functionality from there.
 *      Later, the scope widened to other databases which led to this
 *      version, which is almost a line by line translation of the
 *      original 4GL into Java, (by a Java and OO newbie programmer).
 *      What it needs is to be re-concieved and redesigned from the
 *      ground up, with the 20/20 vision of hindsight... time permitting.
 *      In the mean time, it will be enhanced and debugged as immediate
 *      needs dictate and backward compatibility with existing
 *      driver scripts maintained.
 * ###############################################################
 * Maintenance Programmer notes:
 *      1) The conversion from 4GL to Java, means that array indexes
 *      generally do not follow the Java convention of starting at 0.
 *      WARNING!! Array indexes generally start at 1.
 *      2) At the heart of this program are two arrays and three functions:
 *          s[] and t[]
 *          numeric_diff()
 *          date_diff()
 *          character_diff()
 *      which are driven by one of two possible outer program loops:
 *          main_loop_for_sql()
 *          main_loop_for_file().
 *      The outer loops load fields/columns into the arrays from the
 *      source data, and the target data, work out the data type
 *      and then do the appropriate diff function on the corresponding
 *      array elements.
 *      3) Notational convention for dates in this program follow the
 *          South African English style of: dd/mm/yyyy.
 * ###############################################################
 *  Maintenance: MB. 21/07/2008. Version 18.
 *      This is the Java version which was converted directly from
 *      Informix 4GL.
 *      The one feature that was not working in 4GL is the tolerance.
 *      This has been rewritten in Java, but not yet tested.
 *  Maintenance: MB. 21/07/2008. Version 19.
 *      The need to read from another database manufacturer't database
 *      is seen as the main feature to be delivered in version 19.
 *  Maintenance: MB. 27/03/2008. Version 19
        Traps where the input .SQL file does not
        contain the " as col_synonym" sintax in the final
        select statement.
 *  Maintenance: MB. 27/03/2008. Version 19
        The -H option is an incorrectly enforced requirement
        when a .sql input file is processed with the isKeyColumn
        "-k  1,2,3," optional parameter.
 *  Maintenance: MB. 28/07/2008. Version 20
        Intelligent date format guessing added.
        Many other minor code fixes and improvements.
 *  Maintenance: MB. 30/07/2008. Version 20.4
 *      First production version candidate.
 *  Maintenance: MB. 30/07/2008. Version 21.4
 *      Added local emulation of the non-ascii load and unload
 *      SQL statements provided by the various DB manufacturers.
 *  Maintenance: MB. 30/07/2008. Version 21.5
 *      Cosmetic changes to improve readabilty and semantics.
 *  Maintenance: MB. 14/08/2008. Version 21.6
 *      Added the microsoft driver.
 *      Target table lookup bug fixed by moving targetCursor.next()
 *      from open_cursor_with() into fetch_keyed_row().
 *  Maintenance: MB. 09/09/2008. Version 21.8
 *      Minor error trap information improvements.
 *      Fix the non-termination problem if missing last System.exit(0)
 *  Maintenance: MB. 11/09/2008. Version 21.9
 *      Consolidated separate database connections for meta-data,
 *      source data and target data. Also improved progress reporting.
 *  Maintenance: MB. 10/10/2008. Version 21.10
 *      Fixed a numeric data type identification error for
 *          hyphenated numbers in text. 10/10/2008.
 *  Maintenance: MB. 13/10/2008. Version 21.11
 *      Fixed input file name confusion when using SQL load syntax
 *          in the target SQL file.
 *  Maintenance: MB. 13/10/2008. Version 21.12
 *      Made changes to microsoft connection because unlike Oracle
 *           and Informix, the URL includes the user_id and Password.
 *  Maintenance: MB. 13/10/2008. Version 21.13
 *      Changed the error trap in numeric_diff() to warn and fail-over
 *          to the character_diff() function.
 *  Maintenance: MB. 13/10/2008. Version 21.14
 *      Improved the error trap on Warning 5: and replace null fields
 *          from the DB with the empty string "".  also changed the way
 *          DB errors are reported in the .xls spreadsheet file.
 *  Maintenance: MB. 31/10/2008. Version 21.15
 *      Fixed negative number mis identification as string data.
 *  Maintenance: MB. 03/11/2008. Version 21.16
 *      Fixed absolute tolerance bug with negative numbers.
 *  Maintenance: MB. 04/11/2008. Version 21.17
 *      Allowed the date format guess to fail over to the character_diff()
 *      function  when "Warning 125: Unrecognisable date format: " encountered.
 *  Maintenance: MB. 04/11/2008. Version 21.18
 *      Fixed the split() funtions in the flat file read loop to use the -2
 *      option which recognises trailing null fields
 *      in a delimited string.
 *  Maintenance: MB. 05/11/2008. Version 21.19
 *      Improved info in the help display.
 *      Fixed the percentage difference tolerance to work with negative numbers.
 *  Maintenance: MB. 13/11/2008. Version 22.00
 *      Added the -f dateFormat command line parameter and added formats to
 *          the guessed_date_format() method containing fractions of a second
 *          for a limited number of standardised formats.
 *      Added the [more] option to the -help parameter.
 *  Maintenance: MB. 17/11/2008. Version 22.01
 *      Print the TARGET SQLException in the spreadsheet with a message.
 *      Changed all JDBC connections to TRANSACTION_READ_UNCOMMITTED.
 *  Maintenance: MB. 18/11/2008. Version 22.02
 *      Exception for Oracle only supports TRANSACTION_READ_COMMITTED
 *          as the lowest level.
 *      Improved the date handling capability by including the IBM Informix
 *           DBDATE in the default URL.
 *      Imploved the SQLException handling by including the error number.
 *  Maintenance: MB. 20/11/2008. Version 22.03
 *      Added the functionality to skip the first -j count-number of input
 *          rows to the main_loop_for_sql() function.
 *      Added a call to the new function named primeTheSourceColumnDataTypes()
 *          to the main_loop_for_sql() function.
 *  Maintenance: MB. 21/11/2008. Version 22.04
 *      Debugged the new function named primeTheSourceColumnDataTypes()
 *          to the main_loop_for_sql() function.
 *  Maintenance: MB. 24/11/2008. Version 22.05
 *      Exit the guessDateFormat function early based on
 *          the day of the guessed date being greater than 12.
 *      Added a trap for null in the -v tolerance option.
 *  Maintenance: MB. 25/11/2008. Version 22.06
 *      Update of the -help more facility and the numbering of
 *          un-numbered warnings.
 *  Maintenance: MB. dd/MM/yyy. Version xx.xx
 *      Need to get the target column data type from the target database's
 *          meta data and use this to format and compare the date or a number.
 *      Need to get the latest IBM JVM for Power 6 chips
 *      Need to implement the IBM DB2 JDBC dirivers.
 *      Need to implement the properties class
 *      Need to investigate using the JDBC cursor batch() functions
 *          to improve performance.
 *      Need to implement the Oracle JDBC performance extensions.
 *          http://download.oracle.com/docs/cd/A81042_01/DOC/java.816/a81360/sampapp5.htm
 *          http://download-uk.oracle.com/otn_hosted_doc/jdeveloper/904preview/jdbc-javadoc/oracle/jdbc/OracleConnection.html#setDefaultRowPrefetch(int)
 * ###############################################################
 * Outstanging fixes and kown bugs or additional functionality to be added:
 *      1) Need to be able to process input data-files delimited by
 *          user specified delimiters.
 *          Need to add the ability to process delimited files with
 *          delimitation schemes other than a single character.
 *      2) We need to be able to inherit the user id from the system somehow.
 *      3) Perhaps the password too or the db should allow a null password.
 *      4) Build a GUI user interface for all the parameters. It should
 *      use the "properties" class which has a hierarchical
 *      tree of pages grouping related parameters together and
 *      need to save parameters in the properties file.
 *      5) The gui should have a wizard for first time use and the
 *      properties class after that.
 *      6) The wizard should have the capability to choose audit scenarios
 *       like: File to table; SQL to file; SQL to temp-table; SQL to file.
 *      7) The gui should allow mapping together of source and
 *      target data-columns.
 * ###############################################################
 * References:
 * This is where the root of Suns Java Class library is:
 * http://java.sun.com/j2se/1.4.2/docs/api/allclasses-noframe.html
 * This is where to find a description of how to create Javadoc comments:
 * http://java.sun.com/j2se/javadoc/writingdoccomments/#format
 * How do I convert a String  to an integer with Java? (ie stringToInt()) use eg. Integer.parseInt("23") http://www.devdaily.com/java/edu/qanda/pjqa00010.shtml
 * Netbeans Debugger book: http://books.google.com/books?id=_EVhdPP1L8IC&pg=PA76&lpg=PA76&dq=change+the+value+of+a+variable+in+the+netbeans+debugger&source=web&ots=AVKdJum6ly&sig=jZ8WsY0WxklQ2wiymgdVw9pLnts&hl=en&sa=X&oi=book_result&resnum=2&ct=result#PPA76,M1
 * ###############################################################
 **/
//package metaqa;

/**
 * @author w6000963 Mark Besaans
 */
import java.math.BigDecimal;
import java.util.*;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.io.*;
import java.sql.*;  //import java.sql.Time;
import com.informix.jdbc.*;
import oracle.jdbc.driver.*;
import com.microsoft.sqlserver.jdbc.*;
//import com.ibm.db2.jcc.DB2Connection.*;   //# Does not compile with JDK1.5. It needs JDK1.6
//import java.text.ParseException;
                                            // http://forums.sun.com/thread.jspa?messageID=9943286

public class Metaqa {
    //# General static stuff.
    static final String _version = "metaqa Ver 22.06 released 25/11/2008";
    static Date startTime = new Date();
    static String endTime = null;
    static String _scratch="";                   //# Generic tmp string working with text.
    static String inputKeyCols = null;           //# Contains the commandline Primary keys of -isKeyColumn
    static String inputFileName = "test_data";
    static String userId = System.getProperty("user.name");
    static String interfaceCode = "";
    static String transactionType = "";
    static final char _numeric = 'N';            //# Constant.
    static final char _character = 'C';          //# Constant.
    static final char _date = 'D';               //# Constant.
    static boolean lineHasErr = false;           //# Indicates lines with errors in them.
    static String keyIndYN = "N";                //# "Y" or "N". Kind of Boolian
    static boolean lastRowWasHeader = false;     //# Boolean.
    static boolean numericStringsOn = true;      //# Boolean. ie. 1 = 001.00
    static boolean suppressWarnings = false;     //# Boolean.
    static boolean colNamesInHeader = false;     //# Boolean.
    static boolean percentOn = false;            //# Boolean applies to _tolerance.

    //# General program loop stuff.
    static double _tolerance = 0.0;              //# See percentOn. What difference in numeric values can be tolerated. Absolute diff or percentage diff.
    static boolean _end_of_file = false;         //# Read ascii end of file indicator.
    static int _j = 0;                           //# "FOR" loop counters.
    static int _max_err = 20000;                 //# The maximum number of errors allowed.
    static int _max_line_err = 20000;            //# The max number of lines with errs allowed.
    static int _rowsWithErrors = 0;              //# Count lines with errors in them.
    static int _columnsWithErrors = 0;           //# The errors counted.
    static int _nonKeyColsAudited = 0;           //# The comparisons counted.
    static int _key_count = 0;                   //# How many columns make up the key.
    static int _key_element = 1;                 //# The current Key Element.
    static int src_field_count = 0;              //# How many fields in the input stream, file or SQL.
    static int tgt_col_count = 0;                //# How many columns in the target table.
    static int _rowsChecked = 0;                 //# How many input lines/rows read.
    static int _progress_every = 1000;           //# Show progress every ? input lines.
    static int _jump_over_lines = 0;             //# Skip over this number of nput file lines.
    static int _missingRowsInReverseCompare = 0; //# Total reverse missing keys.

    //# The array structures.
    static int siz = 256;                              //# Standard size of string arrays.
    static int maxKeys = siz;                          //# Maximum number of columns to make up a key.
    static char[] _comparisonType = new char[siz];     //# One of: _numeric; _character; or _date.
    static SimpleDateFormat[] dFmtS =                  //# The Source date format.
                    new SimpleDateFormat[siz];         //# Something like "dd/MM/yy".
    static SimpleDateFormat dFmtT =                    //# The Target date format.
                    new SimpleDateFormat("yyyy-MM-dd");//# SDF of the target DB.
    static String[] _srcMetaType = new String[siz];    //# Data type from the meta data.
    static String[] _srcMetaCol = new String[siz];     //# Data column name from the meta data.
    static String[] _srcMetaTab = new String[siz];     //# Data table name from the meta data.
    static String[] c = new String[siz];               //# Name of column.
    static String[] t = new String[siz];               //# Target data cells.
    static String[] s = new String[siz];               //# Source data cells.
    static boolean[] isKeyColumn = new boolean[siz];   //# Indicator for key element, true / false.
    static String[] p = new String[siz];               //# Key Parameters for target lookup.
    static int[] key_pos = new int[siz];               //# Columnar position of the key in the array.
    static String[] key_pos_str = new String[siz];     //# Columnar position of the key in the array, but the command line string version of this parameter.
    //# NB. Key element 1 may be in column 6 so key elements are mapped.

    static String[] field = new String[siz];           // for backward compatibility with .4gl split().
    static String[] ar = new String[siz];              //# Array of command line Parameters.
    static BigDecimal x = null;                        // Numerics are converted to BigDecimal for comparison.
    static BigDecimal y = null;                        // Numerics are converted to BigDecimal for comparison.
    final static int _not_found = -1;                  //# Returned by String method indexOf().
    static int _num_args = 0;                          // Commandline arguments.

    //# Reverse Compare stuff.
    static boolean reverseCompare = false;             //# Off by default.
    static PreparedStatement reverseKeyUpd = null;     // To save the reverse compare key.
    static PreparedStatement reversePrepedQuery = null;// Reverse compare query.
    static ResultSet reverseCursor = null;             // Reverse compare Cursor.

    //# Output stuff.
    static PrintWriter generatedTargetSqlFile = null;  // Saves the SQL derived by metaqa
    static PrintWriter spreadSheet = null;             //
    static PrintWriter logFile = null;                 //
    static PrintWriter _p_stream = null;               // declare a print stream object
    static BufferedReader sqlInputFile = null;         // The file containing the source SQL targetQuery.
    static BufferedReader flat_input_file = null;      // The input datafile.
    static String defaultJdbcDriver =                  // Which manufacturer's driver.
        "com.informix.jdbc.IfxDriver";                 // com.informix.jdbc.IfxDriver
                                                       // oracle.jdbc.OracleDriver
                                                       // com.microsoft.jdbc.sqlserver.SQLServerDriver
                                                       // com.ibm.db2.jdbc.app.DB2Driver
                                                       // com.ibm.db2.jcc.DB2Driver
    static String defaultJdbcUrl =                     // The database URL of the source data.
       "jdbc:informix-sqli://129.100.1.175:1526/dwh:INFORMIXSERVER=dwh_soc"     //# http://osdir.com/ml/db.squirrel-sql.users/2007-07/msg00006.html
       +";DBDATE=DMY4/;DBCENTURY=C";                   // These two urls alternate with each other because
    // "jdbc:informix-sqli://129.100.1.175:1529/dwh:INFORMIXSERVER=onlsoc1"
    // +";DBDATE=DMY4/;DBCENTURY=C";                   // the DBA's shut them down to fix ODBC Driver problems.
                                                       // jdbc:oracle:thin:@dwhdbdev:1521:dwhdev
                                                       // jdbc:sqlserver://[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
                                                       // jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks;integratedSecurity=true;
                                                       // jdbc:sqlserver://localhost;databaseName=AdventureWorks;integratedSecurity=true;applicationName=MyApp;
                                                       // jdbc:sqlserver://localhost:1433;databaseName=Northwind;user=sa;password=123456;
                                                       // jdbc:db2://localhost:50000/dbname

    //# Meta-data database stuff.
    static Connection metaJdbcConn = null;             // Source DB connection reference.
    static String metaJdbcDriver = defaultJdbcDriver;  // Which manufacturer't driver.
    static String metaJdbcUrl = defaultJdbcUrl;        // The database URL of the source data.
    static String metaJdbcUserId = "mqa";
    static String metaJdbcPassword = "london.";
    static ResultSet metaColCursor = null;             // Cursor for meta-data columns.
    static PreparedStatement metaColExistsPrep = null; // Query to check meta_data.
    static PreparedStatement metaKeyTypePrep = null;   // Query to check key elements in the meta-data.
    static PreparedStatement metaColsNotAuditedPrep = null; // Query for columns not audited in this run.

    //# Source database stuff.
    static Connection sourceJdbcConn = null;       // Source DB connection reference.
    static boolean sqlInput = false;               // If not from SQL then it must be from a local file.
    static String sourceJdbcDriver = defaultJdbcDriver; // Which manufacturer't driver.
    static String sourceJdbcUrl = defaultJdbcUrl;  // The database URL of the source data.
    static String sourceJdbcUserId = "mqa";
    static String sourceJdbcPassword = "london.";
    static ResultSet sourceCursor = null;          // The cursor for source data.
    static String sourceColName = null;            // Source Column Name.
    static String sourceTabName = null;            // Source table Name.
    static String sourceColType = null;            // Source Column Type.
    static String sourceSystem = "MP";             // Source System Code.
    static String sourceDateFormat = null;         // All source date columns will try this first.

    //# Target database stuff.
    static Connection targetJdbcConn = null;       // Target DB connection reference.
    static String targetJdbcDriver =               // Which manufacturer't driver
        defaultJdbcDriver;
    static String targetJdbcUrl =                  // The database URL of the target data.
        defaultJdbcUrl;
    static String targetJdbcUserId = "mqa";        // supp_mb
    static String targetJdbcPassword = "london.";  // please11
    static String targetTableQuery = null;         // Query generated on the target table.
    static PrintWriter generatedSqlFile = null;    // File containing the target targetQuery.
    static PreparedStatement targetQueryPreped = null; // A Prepared targetQuery on the target table.
    static ResultSet targetCursor = null;          // Cursor for the target targetQuery.
    static String targetTable = "MP_CHAIN_SUBCLASS_SN";
    static String targetColumn = null;
    static String targetSystemId = "DWH";          // USed for meta data lookup.
    static String targetDBase = "dwh";             // Depricated. Replaced by JDBC URL.
    static String targetDateFormat = "yyyy-MM-dd"; // All target date columns will try this first.
    static String tempTargetTabSqlFile = "";

/**
 * @param args . Refer to the Usage() method.
 **/
public static void main(String[] args) {   //Top level of program flow.

    System.out.println(_version);

    // Record command line arguments in global structures.
    _num_args = args.length;
    for (int i = 0; i < _num_args; i++) {
        ar[i + 1] = args[i];
    }
    commandline_meta_qa();          // Set program variables by parsing command line args.
    /**
     call user_interface_meta_qa()  // This code will be converted last.
     **/
    try {
        prepare_meta_qa();
        if (sqlInput) {             //# Choose a Main Program Loop.
            main_loop_for_sql();
        } else {
            main_loop_for_file();
        } //end if

        if (reverseCompare) {
            reverse_compare();
        } //end if
        end_main();

    } catch (IOException e) {
        System.out.println("Error 101: main(): "+e);
    } catch (Exception e) {
        System.out.println("Error 102: main(): "+e);
        e.printStackTrace();
        System.exit(102);
    } finally {
        try {
            if (sqlInput) {
                sourceJdbcConn.close();
            }
            targetJdbcConn.close();
            metaJdbcConn.close();
            System.exit(0);
        } catch (SQLException e) {
            System.out.println("Error 128: main(): SQLException ErrCode: "
            +e.getErrorCode()+": "+e);
        } catch (Exception e) {
            System.out.println("Error 129: main(): "+e);
            System.exit(129);
        }
    }
    System.exit(0);
} // end main()


/**
##########################################################
# Purpose: Override defaults with commandline options.
// http://java.sun.com/j2se/1.4.2/docs/api/java/util/Properties.html
##########################################################
 **/
public static void commandline_meta_qa() {
    //# Emulate Informix utilities with a version number.
    if (is_param("-V")) {
        System.exit(0);
    }
    //# Overwrite defaults with command-line parameters and options.
    if (is_param("-D")) {
        try {
            dFmtT = new SimpleDateFormat(get_param("-D"));
        } catch (NullPointerException e) {
            usage();
            System.out.println(
                "Error 1: "+e+" -D refers to a Java SimpleDateFormat: "
                +get_param("-D")+
                "\n\t Try changing it like: -D yyyy-MM-dd or, try using the"+
                "\n\t to_date() or to_char() functions in the source SQL.");
            System.exit(1);
        } catch (Exception e) {
            System.out.println( "Error 118: commandline_meta_qa(): "+e);
            //e.print();
            System.exit(118);
        }
    }
    if (is_param("-H")) { colNamesInHeader      = true; }
    if (is_param("-I")) { interfaceCode         = get_param("-I"); }
    if (is_param("-T")) { targetSystemId        = get_param("-T"); }
    if (is_param("-c")) { tempTargetTabSqlFile  = get_param("-c"); }
    if (is_param("-d")) { targetDBase           = get_param("-d"); }
//  if (is_param("-e")) { targetDateFormat      = get_param("-e"); }  // not implemented.
    if (is_param("-f")) { sourceDateFormat      = get_param("-f"); }
    if (is_param("-h") || is_param("-help")) {
        if (
            get_param("-h"   ).equals("more")  ||
            get_param("-help").equals("more")
           )
        {
            more_help();
            System.exit(0);
        }
        usage();
        System.exit(0);
    }
    if (is_param("-i")) { inputFileName = get_param("-i"); }
    if (is_param("-j")) {
        try {
            _jump_over_lines = Integer.parseInt(get_param("-j"));
        } catch (NumberFormatException e) {
            usage();
            System.out.println("-j refers to non numeric data: "+
                get_param("-j")+". Try changing it like: -j 123");
            System.exit(1);
        } catch (Exception e) {
            System.out.println( "Error 82:  commandline_meta_qa(): "+e);
            //e.printStackTrace();
        }
    }
    if (is_param("-l")) {
        try {
            _max_line_err = Integer.parseInt(get_param("-l"));
        } catch (NumberFormatException e) {
            usage();
            System.out.println("-l refers to non numeric data: " + get_param("-l"));
            System.out.println("Try changing like: -l 123");
            System.exit(2);
        } catch (Exception e) {
            System.out.println( "Error 83:  commandline_meta_qa(): "+e);
            //e.printStackTrace();
        }
    }
    if (is_param("-m")) {
        try {
            _max_err = Integer.parseInt(get_param("-m"));
        } catch (NumberFormatException e) {
            usage();
            System.out.println("-m refers to non numeric data: " + get_param("-m"));
            System.out.println("Try changing like: -m 123");
            System.exit(3);
        } catch (Exception e) {
            System.out.println( "Error 84:  commandline_meta_qa(): "+e);
            //e.printStackTrace();
        }
    }
    if (is_param("-p")) {
        try {
            _progress_every = Integer.parseInt(get_param("-p"));
        } catch (NumberFormatException e) {
            usage();
            System.out.println("-p refers to non numeric data: " + get_param("-p"));
            System.out.println("Try changing like: -p 123");
            System.exit(4);
        } catch (Exception e) {
            System.out.println( "Error 93:  commandline_meta_qa(): "+e);
            //e.printStackTrace();
        }
    }
    if (is_param("-k")) { inputKeyCols = get_param("-k"); }
    if (is_param("-n")) { numericStringsOn = false; }
    if (is_param("-r")) { reverseCompare = true; }
    if (is_param("-s")) { sourceSystem = get_param("-s"); }
    if (is_param("-t")) { targetTable = get_param("-t"); }
    if (is_param("-u")) { userId = get_param("-u"); }
    if (is_param("-v")) {
        String param=get_param("-v");
        if (param == null || param.equals("")) {
            System.out.println(
                "Error 338: The -v option must be followed by a parameter."
                );
            System.exit(1);
        }
        percentOn = (param.lastIndexOf("%") > 0); // Is there a trailing "%" sign?
        _tolerance = Double.valueOf(param.replace("%", "").trim()); // Catch the tolerance regardless of "%" or not.
    }
    if (is_param("-w")) { suppressWarnings = true; }
    if (is_param("-x")) { transactionType = get_param("-x"); }
    //# Source database parameters.
    String[] JDBC = null;
    if (is_param("-Src")) {
        JDBC = get_param("-Src").split(" ");
        if (JDBC.length <= 2 ) {
            System.out.println("Warning 126: Need [-Src SrcDbDriver SrcUrl SrcUserId SrcUserPasswd]. Try with -help.");
        }
        sourceJdbcDriver   = JDBC[0];         // Which manufacturer't driver
        sourceJdbcUrl      = JDBC[1];         // The database URL of the source data.
        if (JDBC.length > 2 ) { sourceJdbcUserId   = JDBC[2]; }
        if (JDBC.length > 3 ) { sourceJdbcPassword = JDBC[3]; }
        JDBC = null;
    }
    //# Target database parameters.
    if (is_param("-Tgt")) {
        JDBC = get_param("-Tgt").split(" ");
        if (JDBC.length <= 2 ) {
            System.out.println("Warning 127: Need [-Tgt TgtDbDriver TgtUrl TgtUserId TgtUserPasswd]. Try with -help.");
        }
        targetJdbcDriver   = JDBC[0];         // Which manufacturer't driver
        targetJdbcUrl      = JDBC[1];         // The database URL of the target data.
        if (JDBC.length > 2 ) {
            targetJdbcUserId   = JDBC[2];
        } else {
            targetJdbcUserId   = null;
        }
        if (JDBC.length > 3 ) {
            targetJdbcPassword = JDBC[3];
        } else {
            targetJdbcPassword = null;
        }
        JDBC = null;
    }
    //# Meta-data database parameters.
    if (is_param("-Met")) {
        JDBC = get_param("-Met").split(" ");
        if (JDBC.length < 2 ) {
            System.out.println("Warning 128: Need [-Met MetDbDriver MetUrl MetUserId MetUserPasswd]. Try with -help.");
        }
        metaJdbcDriver   = JDBC[0];         // Which manufacturer't driver
        metaJdbcUrl      = JDBC[1];         // The database URL of the meta data.
        if (JDBC.length > 2 ) { metaJdbcUserId   = JDBC[2]; }
        if (JDBC.length > 3 ) { metaJdbcPassword = JDBC[3]; }
        JDBC = null;
    }
} //end function { commandline_meta_qa() }

/**
###########################################################
// @Purpose Get parameters following key
// @Return The command line parameter following the key given
//         or null if not there.
// @Bounds Command line arguments are all space separated.
// @See    is_param( key )
###########################################################
 **/
static String get_param(String key) {
    String param = "";
    for (int i=1; i<=_num_args; i++) {   //# Find the key in this loop.
        if (ar[i].equals(key)) {         //# Multiple parameters for this key? ( eg. "HP Laserjet" ).
            i++;
            while (ar[i] != null && !(ar[i].startsWith("-")) && i <= _num_args) {
                param = param + " " + ar[i];
                i++;
            } //end while
            return param.trim(); //return left( param )
        } //end if
    } //end for
    return param.trim();  //return left( param )
} // end function {    get_param( key ) # Formerly get_arg().    }


/**
###########################################################
// Purpose: Determine if <key> is a command line parameter.
// Returns: <the arg number>
// Bounds : command line arguments are all space separated.
// See        : get_param()
// Authors: unknown ( QUANTUM? )
###########################################################
 **/
static Boolean is_param(String key) { //function is_param( key )
    for (short i=1; i<=_num_args; i++) {
        if (ar[i].equals(key)) {
            return true;
        }
    } //end for
    return false;

} //end function {  is_param( key ) # Formerly is_arg().    }


/**
##########################################################
# Purpose: Show the program parameter usage.
##########################################################
 **/
public static void more_help() {

System.out.println( "\n"+centreLineOf(" More help for meta_qa ","-",80)
+"\nPurpose:"
+"\n    Compares a data SOURCE with a data TARGET to determine differences."
+"\n    Internally generates a query on the specified TARGET table, by using"
+"\n    columns that contain the same data (as defined by meta data - see below)."
+"\nInput:"
+"\n    1) Gets SOURCE data input from a delimited flat file, or"
+"\n    2) by running database SQL statements from a .SQL file"
+"\n       (denoted with the -i option) against a TARGET table in a"
+"\n       TARGET database. These TARGET tables can be temporary tables,"
+"\n       created by processing SQL statements from a second .SQL file,"
+"\n       (denoted by the -c option), so that one of those temp tables"
+"\n       can be used as the TARGET table."
+"\n    3) Gets META data from the META_DWH_TABLE_FIELD table (if it exists)."
+"\nOutput is always:"
+"\n    1) It produces a tab delimited spreadsheet file with a .xls extension."
+"\n    2) The internally generated query is written to a file for your"
+"\n       convenience. Its name begins with the table name,"
+"\n       and ends in the string '.meta_qa.sql'."
);
System.out.println(
   "Data sources can be one of: "
+"\n    1) Delimited flat files visible from the file system, and/or"
+"\n    2) Tables in any SQL compliant database accessible on the network."
+"\n       This is achieved through the use of JDBC drivers, one for each DB,"
+"\n       and the SQL query language statements suited to that DataBase."
+"\n       Refer to the -help option for example JDBC parameter details."
+"\nLight-weight non-locking strategey:"
+"\n    Our JDBC connections use (non-locking) 'dirty read' or"
+"\n    TRANSACTION_READ_UNCOMMITTED. This is especially true for Microsoft"
+"\n    SQLserver which 'locks on read' by default. Oracle 11g only supports the"
+"\n    more complex (READ_COMMITTED and SERIALIZABLE), so we use the former."
+"\nMeta data:"
+"\n    Is used internally to link a column in a SOURCE table, in a SOURCE"
+"\n    database, to a TARGET column in a TARGET table in a TARGET database."
+"\n    This link is used to generate the TARGET query and also for information"
+"\n    in the .xls file (when variances are detected). If there is no meta-data"
+"\n    for the data comparison you wish to perform, then meta_qa needs one of"
+"\n    two other mechanisms for linking/mapping SOURCE columns and TARGET columns."
);
System.out.println(
   "Column mapping mechanisms:"
+"\n    The SOURCE and TARGET columns can be mapped to each other"
+"\n    by one of the following three mechanisms:"
+"\n    1) If the SOURCE data is in a delimited flat file and there is meta"
+"\n       data in a META_DWH_TABLE_FIELD table, then the meta-data can "
+"\n       be used to map source columns to target columns. The columns"
+"\n       must be in the same order as the 'design_sequence' of"
+"\n       META_DWH_TABLE_FIELD and all the columns for the"
+"\n       source/destination/system/table combination must be"
+"\n       present in the delimited file."
+"\n     2) If the data SOURCE is a delimited flat file and there is NO meta"
+"\n       data in a META_DWH_TABLE_FIELD table, then the target column names can"
+"\n       be placed, (in their matching order), in a pipe delimited"
+"\n       header record. eg. 'HEADER|LOCATION|DATE|PRODUCT|SALES|...|COST|'."
+"\n       This mechanism is (denoted by the -H command line option)."
+"\n     3) If the SOURCE data is fetched by a query in a .SQL file,"
+"\n       (denoted by the -i option), then the column mapping is done"
+"\n       by using the 'AS COLUMN-SYNONYM' syntax of the 'SELECT' clause."
+"\n     4) An unimplemented fourth possibility would be a combination of: 1)"
+"\n       and 2), where meta data from the table META_DWH_TABLE_FIELD is"
+"\n       used to link/map columns from the SOURCE query to the TARGET"
+"\n       table in the same sequence as the TARGET columns."                   //# Would require a SQL parser.
+"\nKey Columns:"
+"\n     1) If the META_DWH_TABLE_FIELD table contains the columns for the"
+"\n       TARGET table and the column \"PRIMARY_KEY\" is set to 1, then"
+"\n       meta_qa can use this to identify the keys when building the"
+"\n       where clause. This is the default and you do not need to"
+"\n       specify the -k option."
+"\n     2) Else, use the -k option followed by a comma separated list"
+"\n       of column numbers, and then meta_qa will use the names of"
+"\n       these columns in the TARGET SQL query."
);
System.out.println(
   "Data Types:"
+"\n    All fields are read in as strings.  Therefore, in order to compare"
+"\n    reciprocal fields, that are not strings, like dates and numbers,"
+"\n    meta_qa has to convert them to the Java Date type or the bigNumber"
+"\n    class."
+"\n    1) Dates:"
+"\n      i) Dates can arrive in over 100 differnt formats, so two identical"
+"\n         dates can look very differnt in human readable string form. Thus:"
+"\n         dates are handled 'intelligently'. By accepting formats from the"
+"\n         command-line, and also by scanning the first row/line of data from"
+"\n         both the SOURCE and the TARGET, meta_qa looks for fields containing"
+"\n         dates and makes a best guess at the date-format by trying to convert"
+"\n         the string date into an internal binary date, using each of a host"
+"\n         of different formats, until one of them is successful."
+"\n       ii) Conversely, if a KEY-column is a date, then the exact date"
+"\n         string from the SOURCE is passed, as-is, to the WHERE clause of the"
+"\n         TARGET query.  Thus the format of the date in the SOURCE data key"
+"\n         must match the default date format of the TARGET database."
+"\n         Refer to the -D and -f command-line options."
+"\n    2) Numbers are simpler as there are far fewer ways to represent them,"
+"\n       but none the less, the same number can be represented in different"
+"\n       ways in a string and therefore need to be converted.  The BigNumber"
+"\n       Java Class is used to standardise all numbers before comparing them."
+"\n    3) If a field neither qualifies as string nor number, then the default"
+"\n       data type remains a string, and the two strings will"
+"\n       be compared without data type conversion."
);
System.out.println(
   "Application scenarios:"
+"\n    Metaqa will compare data in the following scenarios:"
+"\n    1) A pipe delimited input flat file to a TARGET table."
+"\n    2) A .SQL query to a TARGET table."
+"\n    3) A .SQL query to a temp TARGET table (dynamically created"
+"\n       by a second .SQL file)."
+"\n    4) A delimited flat file to a second delimited flat file:"
+"\n       The SQL load command is supported by meta_qa. Thus comparing one"
+"\n       delimited flat file to second delimited flat file, can be"
+"\n       achieved in one of the following two ways:"
+"\n       i) Create a temp TARGET table, load it with the SQL 'load' statement"
+"\n          in one .SQL file (denoted by the -c option) while"
+"\n          in a second .SQL file, (denoted by the -i option), you create and"
+"\n          load a second temp table, again using the 'load' statement,"
+"\n          followed by a SQL select statement, which matches the target table."
+"\n       ii) Create a temp TARGET table as in i) above, but for input,"
+"\n          use a pipe delimited file, (denoted with the -i option),"
+"\n          with a HEADER record containing the names of the TARGET columns"
+"\n          (to be used in the process of generating the TARGET query)."
+"\n"+centreLineOf(" End of more help for meta_qa ","-",80)
);
} //end function {  more_help()  }


/**
##########################################################
# Purpose: Show the program parameter usage.
##########################################################
 **/
public static void usage() {

System.out.println(
//"Usage: java -jar metaqa.jar [-h] [-help] [-s src-sys] [-t table] [-i input-file] [-b] [-H [-k 1,2,3,...,n,]] [-n] [-v tolerance[%]] [-m max-errs] [-l line-err-max] [-p progress] [-u user-id] [-I interface-no] [-x ait-transaction-type] [-r] [-c file] [-Src SrcDbDriver SrcUrl SrcUserId SrcUserPasswd] [-Tgt TgtDbDriver TgtUrl TgtUserId TgtUserPasswd] [-Met MetDbDriver MetUrl MetUserId MetUserPasswd]"
"Usage: java -jar metaqa.jar [-h [more]] [-help [more]] [-s src-sys] [-t table] [-i input-file] [-b] [-H [-k 1,2,3,...,n,]] [-v tolerance[%]] [-c file] [-n] [-r] [-f dateformat] [-m max-errs] [-l line-err-max] [-p progress] [-u user-id] [-I interface-no] [-x ait-transaction-type] [-Src SrcDbDriver SrcUrl SrcUserId SrcUserPasswd] [-Tgt TgtDbDriver TgtUrl TgtUserId TgtUserPasswd] [-Met MetDbDriver MetUrl MetUserId MetUserPasswd]"
+"\n   -H                Get the column names from the input-file 'HEADER|'."
+"\n   -I interface-no   The unique Meta Data Interface number."
+"\n   -T target-system  Defaults to DWH and is a Pkey element for the user"
+"\n                     defined meta data table META_DWH_TABLE_FIELD index."
+"\n   -V                Prints the meta_qa version number only."
+"\n   -b                Batch mode only. Bypasses the user interface."
+"\n   -c sql-file.sql   Create and load a temp TARGET table from a .SQL file."
+"\n   -d db             Database name of TARGET system. Defaults to 'dwh',"
+"\n   -D DateFormat     The format of dates produced by the TARGET dBase. For"
+"\n                     Informix, export DBDATE='Y4MD-'. The format can "
+"\n                     contain the time: eg. 'yyyy-MM-dd HH:mm:ss.SSS'. If"
+"\n                     the data contains the time, but the format does not, the"
+"\n                     date will be parsed correctly without the time format."
+"\n                     The Informix date format can also be controlled via the"
+"\n                     JDBC url. Refer to the default -Tgt url as an example."
+"\n   -f DateFormat     The SOURCE data SimpleDateFormat. Default is to guess"
+"\n                     it. See the -D option for more detail. The Oracle JDBC"
+"\n                     driver ignores all format instructions like this SQL:"
+"\n                     ALTER SESSION SET NLS_DATE_FORMAT='YYYY-MM-DD'; Try this: "
+"\n                     select to_char(sysdate, 'dd/mmm/yyyy hh24:mi:ss') from dual;"
+"\n                     On Informix, use: to_char(A.tran_date,'%d/%b/%y')"
+"\n   -h more           Gives more help about using this program called metaqa."
+"\n   -i input-file     A delimited flat-file, eg. 'mp.dat'; OR"
+"\n                     a .sql file of SQL statements, ending in a query."
+"\n   -j jump-count     Ignores the first <count> number of input records/rows."
+"\n   -k 1,2,3..,n      The primary-key columns of the TARGET table in any order."
+"\n                     NB. Specify -H if -k is used with a delimited file."
+"\n   -l line-err-max   Defaults to 20000 lines of errors."
+"\n   -m max-errs       Defaults to 20000 errors in total."
+"\n   -n                Don't treat numeric text as numbers.(Eg. 1!=0001)."
+"\n   -p progress       Show progress after every 500? number of input lines."
+"\n   -r                Also reverse compare from TARGET to SOURCE."
+"\n   -s source-system  Defaults to MP. and is a key in the user defined"
+"\n                     meta data table META_DWH_TABLE_FIELD."
+"\n   -t target-table   Defaults to MP_CHAIN_SUBCLASS_SN and is a key in the"
+"\n                     user defined meta data table META_DWH_TABLE_FIELD."
+"\n   -u user-id        The unix user_id." );
System.out.println(
   "   -v tolerance[%]   The absolute numeric variance that will be tolerated"
+"\n                     either as a positive (absolute value or a percentage)."
+"\n                     The percentage difference formula =abs(((x-y)/x))*100)"
+"\n   -w                Suppress warnings on column count differences and on"
+"\n                     TARGET cursor lookups, etc."
+"\n   -x ait-trans-type The Transaction Type, unique per AIT Interface."
+"\n -Src SrcDbDriver    The SOURCE JDBC DB driver."    +" Defaults to: "+defaultJdbcDriver
+"\n      SrcUrl         The SOURCE JDBC url."          +" Defaults to: "+defaultJdbcUrl
+"\n      SrcUserId      The SOURCE JDBC DB User Id."   +" Defaults to: "+metaJdbcUserId
+"\n      SrcUserPasswd  The SOURCE JDBC User Password."+" Defaults to: "+metaJdbcPassword
+"\n -Tgt TgtDbDriver    The TARGET JDBC DB driver."    +" Defaults to: "+defaultJdbcDriver
+"\n      TgtUrl         The TARGET JDBC url."          +" Defaults to: "+defaultJdbcUrl
+"\n      TgtUserId      The TARGET JDBC User Id."      +" Defaults to: "+metaJdbcUserId
+"\n      TgtUserPasswd  The TARGET JDBC User Password."+" Defaults to: "+metaJdbcPassword
+"\n -Met MetDbDriver    The meta-data JDBC DB driver." +" Defaults to: "+defaultJdbcDriver
+"\n      MetUrl         The meta-data JDBC url."       +" Defaults to: "+defaultJdbcUrl
+"\n      MetUserId      The meta-data JDBC User Id."   +" Defaults to: "+metaJdbcUserId
+"\n      MetUserPasswd  The meta-data JDBC User Password. Defaults to: "+metaJdbcPassword
+"\n"
+"\n  Other limits: keys="+maxKeys+"; Columns="+siz+"; Errors=2147483647;"
+"\n                Line-errors=2147483647"
);
} //end function {  usage()  }


//------------------- Command Line initialization ends here --------------------


/**
##########################################################
# Purpose: Do the reverse compare from table to file on unique key.
#    Note: If the 20 key element limitation is changed, do it here too.
##########################################################
**/
public static void reverse_compare() {

    try {
        System.out.println(DateUtils.now("HH:mm:ss")+         //# "yyyy-MM-dd HH:mm:ss"
                " Reverse compare started.");
        reverseCursor = reversePrepedQuery.executeQuery();    //# On targetJdbcConn<ection>
        while (reverseCursor.next()) {
            //# Report missing from source.
            _missingRowsInReverseCompare++ ;
            String str = "";
            for (int i=1; i<=_key_count; i++) {               //# The key from source.
                str += reverseCursor.getString(i)+ "\t";
            } //end for
            str += "NoSource";   //# Preserve the tab.;
            spreadSheet.println( str );
            max_err_exit();
        } //end while
        System.out.println( DateUtils.now( "HH:mm:ss")+       //# Not "yyyy-MM-dd HH:mm:ss"
                " Reverse compare ended." );
    } catch (SQLException e) {
        System.out.println(
            "Error 119: reverse_compare(): SQLException ErrCode: "
            +e.getErrorCode()+": "+e);
        //e.printStackTrace();
    } catch (Exception e) {
        System.out.println( "Error 120: reverse_compare(): "+e);
        //e.printStackTrace();
    }
} //end function { reverse_compare( n ) }


/**
 * ##########################################################
 * Purpose: Contains all the code that reads a line
 *          from the source data flat file into an array.
 * ##########################################################
 * @return
 */
public static String read_flat_file_line_into_array() {
    String sourceLine = null;
    try {
        sourceLine = flat_input_file.readLine();
        if (sourceLine == null){
            return sourceLine;
        }
        String[] inField = sourceLine.trim().split("[|]",-2);
        src_field_count = inField.length;
        for (int i=src_field_count;i>=1;i--) {
            s[i]=inField[i-1];      // Ripple the array to start at index 1.
        } // end for
    } catch (IOException e) {
        System.out.println( "Error 105: read_flat_file_line_into_array(): "+e);
    }
    return sourceLine;
} //end function read_flat_file_line_into_array()


/**
##########################################################
# Purpose: Drive the main program loop for input file reading.
##########################################################
**/
public static void main_loop_for_file() { //function main_loop_for_file()
    String dataLine=null;
    int min = 0;
    _rowsChecked = 0;
    try {
        System.out.println(
            DateUtils.now("HH:mm:ss")+ //"yyyy-MM-dd HH:mm:ss"
            " Reading source data-file: "+ inputFileName.trim()
            );
        //# Do another priming read after reading the HEADER.
        if (lastRowWasHeader) {
            dataLine = read_flat_file_line_into_array();
        } //end if
        primeTheSourceColumnDataTypes();
        //# Expecting data from here on.
        while (dataLine != null) {      //.4GL while not _end_of_file
            lineHasErr = false;         //# Initialise error indicator for this input line.
            //# Handle the trailer.
            if (dataLine.startsWith("TRAILER|")) {
                System.out.println("Trailer total: "+ s[2]+
                    " Lines counted: "+ _rowsChecked);
                dataLine = read_flat_file_line_into_array();
                continue; // while
            } //end if

            //# The line count is here in order to ignore the header and trailer.
            _rowsChecked++;

            //# Jump over the number of input lines specified.
            if (_rowsChecked <= _jump_over_lines) {
                continue; // while
            } //end if

            //# Trap number of columns error.
            if (src_field_count != tgt_col_count & (! suppressWarnings)) {
                System.out.println(" Warning 349: Line: "+_rowsChecked+
                    " The SOURCE and TARGET column counts differ respectively: "+
                     src_field_count+ ", "+ tgt_col_count);
            } //end if

            //# Order the keys to open the cursor with. ###NB. There is opportunity to improve performance here by going directly to the columns in s[].
            for (int i=1; i<=_key_count; i++) {
                p[i] = s[key_pos[i]];     //###MB### If the field is a key and == null???;
            } //end for

            //# Record the unique key of every line from the file in a temp table.
            if (reverseCompare) {
                save_key_for_reverse_compare(_key_count);
            } //end if

            //# Do the lookup into the target table.
            if ((! open_cursor_with( _key_count ))) {
                dataLine = progress_check_and_read();
                continue; // continue while
            } //end if
            if (fetch_keyed_row(dataLine) == false) {
                report_missing("Missing", null);
                dataLine = progress_check_and_read();
                continue; // continue while
            } //end if

            if (_rowsChecked == 1) {
                primeTheTargetColumnDataTypes(); //# Do it once only, because it's a costly exercise.
            }

            //# Compare corresponding SOURCE and TARGET columns.
            min =  (src_field_count<tgt_col_count ? src_field_count : tgt_col_count );
            for (int i=1; i<=min; i++) {
                if (isKeyColumn[i]) {                //# Skip over key elements.
                    continue; // continue for         //Note: Performance can be improved here by implementing a hashmap to columns that need to be audited.
                } //end if
                if ( c[i].equals("\"\"") ||  c[i].equals("''")  ) {   //# Skip over null named columns.
                    continue; // continue for
                } //end if
                _nonKeyColsAudited++;
                switch (_comparisonType[i]) {
                    case _numeric   : numeric_diff(i); break;
                    case _date      : date_diff(i);    break;
                    case _character : clever_diff(i);  break;
                } // end switch
            } //end for
            dataLine = progress_check_and_read();
        } //end while
    } catch (Exception e) {
        System.out.println( "Error 74: main_loop_for_file(): "+e);
    } finally { }
} //end function { main_loop_for_file() }


/**
##########################################################
# Purpose: Do an alpha-numeric comparison.
# The statements below produce NullPointerException
# if (t[i].equals("") & s[i] == null)    return;
# if (t[i] == null    & s[i].equals("")) return;
##########################################################
 *
 * @param i This is the comparable column number
 *          of two fields that should be equal.
 */
public static void character_diff(int i) { //function character_diff()
    try {
        if (isNullDiff(i)) {                       //# Handle null/empty string combinations.
            return;
        }
        if (!(t[i].trim().equals(s[i].trim()))) {  //# They are simply different.
            report_inequality( i );
            return;
        } //end if
        if (t[i].matches("[ ][ ]*") &              //# Both are full
            s[i].matches("[ ][ ]*") &              //# of spaces and
            t[i].length() != s[i].length()         //# different lengths.
        ) {
            report_inequality( i );
            return;
        } //end if
    } catch (Exception e) {
        System.out.println("Error 112: "+e+":\n\t"+t[i]+" & "+s[i]+
            " Input row: "+_rowsChecked+" col: "+i+
            " Col-name: "+c[i] );
        System.exit(112);
    }
    return;
} //end function {  character_diff()  }


/**
##########################################################
# Purpose: Run through the first line or row of data
#          working out the data-comparison-type of each column.
#          Meant to be done on the first data line only...
#        : This may be a good place to hook in a look-up
#          into the target meta data to get the data type.
 * Accessing the DB meta data was possible but not advised in 4gl.
 * The tricky part is guessing the java simpleDateFormat.
##########################################################
**/
public static void primeTheSourceColumnDataTypes() {
    try {
        for (int i=1; i<=tgt_col_count; i++) {
            if (is_date( s[i] ) || c[i].matches(".*[Dd][Aa][Tt][Ee].*")) {
                dFmtS[i] = guessed_date_format(s[i]);
                _comparisonType[i] = _date;
            } else if (is_numeric( s[i] )) {
                _comparisonType[i] = _numeric;
            } else {
                _comparisonType[i] = _character;
            } //end if
        } //end for
    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        // Do nothing.
    }
    return;
} //end function primeTheSourceColumnDataTypes()


/**
##########################################################
# Purpose: Run through a target row of data,
#          working out the data-comparison-type of each column.
#          The last format found will apply to all TARGET dates.
# Thought: This may be a good place to hook in a look-up
#          into the target meta data to get the data type.
 * Accessing the DB meta data was possible but not advised in 4gl.
 * The tricky part is guessing the java simpleDateFormat.
##########################################################
**/
public static void primeTheTargetColumnDataTypes() {
    SimpleDateFormat theGuessedDate_format = null;
    String theGuessDateString = "";
    String theGuessedName = "";
    int    theGuessedCol = 0;
    int    i=0;
    try {
        for (i=1; i<=tgt_col_count; i++) {
            //# The last format found will apply to all TARGET dates. @todo use JDBC meta-data.
            if (c[i].matches(".*[Dd][Aa][Tt][Ee].*")) {
                _comparisonType[i] = _date;
                if (t[i] == null) { continue;}
                if (is_date( t[i] )) {
                    theGuessedDate_format = guessed_date_format(t[i]);
                    theGuessDateString = t[i];
                    theGuessedCol  = i;
                    theGuessedName = c[i];
                } else {
                    //# Thus the existing TARGET date format persists.
                } //end if
                try {
                    // The flaw in the guess process is months and days below 13 can be interchaged.
                    // Get out of the loop if the day is greater than the max month of 12.
                    Date realDate = theGuessedDate_format.parse(theGuessDateString);
                    if (Integer.parseInt(DateUtils.day(realDate,"dd"))>12) {
                        break; // for loop
                    }
                } catch (java.text.ParseException e) {
                    System.out.println("Error 138: "+e);
                    System.exit(138);
                }
                continue; // for loop
            } //end if
            if (is_numeric( t[i] )) {
                _comparisonType[i] = _numeric;
            } else {
                _comparisonType[i] = _character;
            } //end if
        } //end for

        //# If the command-line date format and the guessed date format are different, choose the guessed one.
        if (
            (theGuessedDate_format != null) &&
            !(dFmtT.toPattern().equals(theGuessedDate_format.toPattern()))
            )  {
            System.out.println(DateUtils.now("HH:mm:ss")+ //"yyyy-MM-dd HH:mm:ss"
                " Changing the -D target date format from: "+dFmtT.toPattern()+
                " to: "           +theGuessedDate_format.toPattern()+
                "\n\t used date: "+theGuessDateString+
                " in column: "    +theGuessedCol+
                " named: "        +theGuessedName+
                " to guess the date format."
                );
            dFmtT = theGuessedDate_format;
        }
    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        // Do nothing.
    }
    return;
} //end function primeTheTargetColumnDataTypes()


/**
*##########################################################
*# Purpose: Determine if a string contains a date.
*# Returns True if the string is like a date else False
*# eg. 2008-01-31 or 31/01/2008 20:30:05 results in true.
*# See: guessed_date_format()
*##########################################################
 *
 * @param d
 * @return
 */
public static boolean is_date(String d) { //function is_date(d)
boolean b = false;
    try {
        if (d == null || d.equals("")) { //Evaluste from left to right and exit early.
            return false;
        }
        //# This is probably a date if it passes this test.
        b = d.matches(".*[0-9][0-9][-/][0-9][0-9][-/][0-9][0-9].*");
    } catch (Exception e) {
        if (! suppressWarnings) {
            System.out.println(" Warning 337: "+e);
        } //end if
    } finally {}
return b;
} //end function {  is_date()  }


/**
 *########################################################
 * Purpose: Create array of possible simplpe date formats.
 * Abandonned.
 *########################################################
 *
 * @return
 */
public static String[] possible_date_formats() {
    String[] dateFormats = new String[500];
     char[] dateSep = {'/', '-', ' '};
    String[] [] dmyPos = new String[500] [3];
    dmyPos[1][1] = "dd"; dmyPos[1][2] = "MM"; dmyPos[1][3] = "yy";
    dmyPos[2][3] = "dd"; dmyPos[2][1] = "MM"; dmyPos[2][2] = "yy";
    dmyPos[3][2] = "dd"; dmyPos[3][3] = "MM"; dmyPos[3][1] = "yy";
    dmyPos[4][1] = "dd"; dmyPos[4][3] = "MM"; dmyPos[4][2] = "yy";
    dmyPos[5][2] = "dd"; dmyPos[5][1] = "MM"; dmyPos[5][3] = "yy";
    dmyPos[6][3] = "dd"; dmyPos[6][2] = "MM"; dmyPos[6][1] = "yy";
    dmyPos[7][1] = "dd"; dmyPos[7][2] = "MM"; dmyPos[7][3] = "yyyy";
    dmyPos[8][3] = "dd"; dmyPos[8][1] = "MM"; dmyPos[8][2] = "yyyy";
    dmyPos[9][2] = "dd"; dmyPos[8][3] = "MM"; dmyPos[9][1] = "yyyy";
    dmyPos[10][1] = "dd"; dmyPos[10][3] = "MM"; dmyPos[10][2] = "yyyy";
    dmyPos[11][2] = "dd"; dmyPos[11][1] = "MM"; dmyPos[11][3] = "yyyy";
    dmyPos[12][3] = "dd"; dmyPos[12][2] = "MM"; dmyPos[12][1] = "yyyy";

    SimpleDateFormat sdf = null;
    // Build up the date portions.
    int j=1;
    for (int i=1;i<=3;i++) {
        for (char dS : dateSep) { //# New type of "for" loop.
            dateFormats[j++] = "dd"+dS+"MM"+dS+"yy";
        }
    }
    return dateFormats;
}

/**
*##########################################################
*# Purpose: Guess the format of a date.
*# Returns: SimpleDateFormat or null.
*# http://forums.sun.com/thread.jspa?messageID=9448242
*# New "for" loop. http://www.leepoint.net/notes-java/flow/loops/foreach.html
*##########################################################
 *
 * @param dateString
 * @return
 */
public static SimpleDateFormat guessed_date_format(String dateString) {
    //example. dateString = "Jan 3, 2007 23:59";
    if ((dateString == null) || dateString.equals("")) { return null; }
    SimpleDateFormat gotIt = null;
    //# Return if the command line parameter date format is set correctly for this date.
    if (sourceDateFormat != null ) {
        try {
            gotIt = new SimpleDateFormat(sourceDateFormat);
        } catch ( Exception e) {
            System.out.println("Error 133: guessed_date_format(). "+e+
                " The command line option -f "+sourceDateFormat.trim()+
                " can not be parsed as a Java simpleDateformat.\n"+
                "You can try using the date help provided by the -help option.");
            System.exit(133);
        }
        gotIt = areTheseCompatible(sourceDateFormat, dateString );
        if (gotIt != null) {
            return gotIt;
        }
    }
    // NB. Fractions of a second SSS are only implemented here with descending order formats.
    //set2DigitYearStart(new SimpleDateFormat("dd/MM/yyyy").parse("31/12/1950")); //# http://forums.sun.com/thread.jspa?threadID=481495&messageID=2244949
    String[] dateFormats = { //# Array of the most common date/time formats.
        "dd/MM/yy", "yy/MM/dd", "dd/MMM/yy", "yy/MMM/dd",                   // Year as two digits.
        "dd/MM/yy HH:mm:ss", "yy/MM/dd HH:mm:ss", "dd/MMM/yy HH:mm:ss",
        "yy/MMM/dd HH:mm:ss", "dd/MM/yy ss:mm:HH", "yy/MM/dd ss:mm:HH",
        "dd/MMM/yy ss:mm:HH", "yy/MMM/dd ss:mm:HH",
        "dd/MM/yy HH:mm", "yy/MM/dd HH:mm", "dd/MMM/yy HH:mm",
        "yy/MMM/dd HH:mm", "MMM d, yy HH:mm", "MMM dd, yy HH:mm",           // Space separators.
        "MMM d, yy H:mm", "MMM dd, yy H:mm", "d MMM yy HH:mm",
        "dd MMM yy HH:mm", "d MMM yy H:mm", "dd MMM yy H:mm",
        "MMM d, yy", "MMM dd, yy", "MMM d, yy", "MMM dd, yy",
        "d MMM yy", "dd MMM yy", "d MMM yy", "dd MMM yy", "dd-MM-yy",       // Dash separators.
        "yy-MM-dd", "dd-MMM-yy", "yy-MMM-dd", "dd-MM-yy HH:mm:ss",
        "yy-MM-dd HH:mm:ss", "dd-MMM-yy HH:mm:ss", "yy-MMM-dd HH:mm:ss",
        "dd-MM-yy ss:mm:HH", "yy-MM-dd ss:mm:HH", "dd-MMM-yy ss:mm:HH",
        "yy-MMM-dd ss:mm:HH", "dd-MM-yy HH:mm", "yy-MM-dd HH:mm",
        "dd-MMM-yy HH:mm", "yy-MMM-dd HH:mm",
        "dd/MM/yyyy", "yyyy/MM/dd", "dd/MMM/yyyy", "yyyy/MMM/dd",           // Year as four digits.
        "dd/MM/yyyy HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss.S",
        "yyyy/MM/dd HH:mm:ss.SS", "yyyy/MM/dd HH:mm:ss.SSS", "dd/MMM/yyyy HH:mm:ss",
        "yyyy/MMM/dd HH:mm:ss", "dd/MM/yyyy ss:mm:HH", "yyyy/MM/dd ss:mm:HH",
        "dd/MMM/yyyy ss:mm:HH", "yyyy/MMM/dd ss:mm:HH",
        "dd/MM/yyyy HH:mm", "yyyy/MM/dd HH:mm", "dd/MMM/yyyy HH:mm",
        "yyyy/MMM/dd HH:mm", "MMM d, yyyy HH:mm", "MMM dd, yyyy HH:mm",     // Space separators.
        "MMM d, yyyy H:mm", "MMM dd, yyyy H:mm", "d MMM yyyy HH:mm",
        "dd MMM yyyy HH:mm", "d MMM yyyy H:mm", "dd MMM yyyy H:mm",
        "MMM d, yyyy", "MMM dd, yyyy", "MMM d, yyyy", "MMM dd, yyyy",
        "d MMM yyyy", "dd MMM yyyy", "d MMM yyyy", "dd MMM yyyy", "dd-MM-yyyy", // Dash separators.
        "yyyy-MM-dd", "dd-MMM-yyyy", "yyyy-MMM-dd", "dd-MM-yyyy HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss.S", "yyyy-MM-dd HH:mm:ss.SS", "yyyy-MM-dd HH:mm:ss.SSS",
        "dd-MMM-yyyy HH:mm:ss", "yyyy-MMM-dd HH:mm:ss",
        "dd-MM-yyyy ss:mm:HH", "yyyy-MM-dd ss:mm:HH", "dd-MMM-yyyy ss:mm:HH",
        "yyyy-MMM-dd ss:mm:HH", "dd-MM-yyyy HH:mm", "yyyy-MM-dd HH:mm",
        "dd-MMM-yyyy HH:mm", "yyyy-MMM-dd HH:mm"
    };
    int i = 0;
    for (String dF : dateFormats) { //# New type of "for" loop.
        gotIt = areTheseCompatible(dF, dateString );
        i++; // debug line to show the array element number.
        if (gotIt != null) {
            return gotIt;
        }
    } // end for
    //String strOutDt = new SimpleDateFormat("mm/dd/yyyy").format(realDate);
    return gotIt;
} //end function {  guessed_date_format()  }

/**
##########################################################
# Purpose: Compare the two current-index array elements.
# Parameters: An integer value for the array index.
# ReferTo: http://forum.java.sun.com/thread.jspa?threadID=791040&messageID=4495425
##########################################################
 *
 * @param i
 */



/**
##########################################################
# Purpose: Workings of the date guess loop.
# Parameters: An integer value for the array index.
# ReferTo: http://forum.java.sun.com/thread.jspa?threadID=791040&messageID=4495425
##########################################################
 *
 * @param i
 * @param dF
 * @param dateString
 * @return
 */
public static SimpleDateFormat areTheseCompatible(String dF, String dateString ) { //function areTheseCompatible()
    SimpleDateFormat theFormat = null;
    try {
        //# Forward and backward convert the date to see if it remains the same with this dF.
        theFormat = new SimpleDateFormat(dF);
        Date realDate = theFormat.parse(dateString);
        String sF = theFormat.format(realDate);
        if (sF.equals(dateString)) {
            //dateFormats = null;           // Save some memory.
            sourceDateFormat = dF;          // Debug line.
            return theFormat;
        } else {
            theFormat = null;
        } //end if
    } catch ( java.text.ParseException e) { // Keep looping on error.
        theFormat = null;
    } catch ( Exception e) {
        System.out.println("Error 114: in guessed_date_format(): "+e
            +" dateString: "+dateString+" simpleDateFormat: "+dF);
        System.exit(114);
    } //end catch
    return theFormat;
}//end function areTheseCompatible()


/**
##########################################################
# Purpose: Compare the two current-index array elements.
# ReferTo: http://forum.java.sun.com/thread.jspa?threadID=791040&messageID=4495425
##########################################################
 *
 * @param i An integer value for the array index.
 */
public static void date_diff(int i) { //function date_diff()
    //String debug = c[i];                   //# Check which column we are working with here.
    try {
        if (isNullDiff(i)) {
            // isNulldiff() caters for null and empty string combinations.
        } else {
            if (dFmtS[i] == null) {
                dFmtS[i] = guessed_date_format(s[i]);
                if (dFmtS[i] == null) {
                System.out.println(" Warning 125: Unrecognisable date format: "+
                    s[i]+ " Line: "+_rowsChecked+" Column: "+i+" Treating it as character data.");
                _comparisonType[i] = _character; //# From this point on treat the column as character data.
                character_diff( i );
                return;
                }
            }                                    //# The target only has one date format.
//    try {
//    } catch (Exception e) {
//    } //end catch
            Date dx = dFmtT.parse(t[i]);         //# Convert based on the
            Date dy = dFmtS[i].parse(s[i]);      //# pre-determined date-format.
            if ( dx.compareTo(dy) != 0 ) {       //# The dates differ?
                    report_inequality( i );
            } //end if
        } //end if
    } catch (NullPointerException e) {
        System.out.println("Error 124: date_diff(): "+e+
            "\n\t Input row: "+_rowsChecked+" col: "+i+
            " Col-name: "+c[i]+
            "\n\t Comparing target: "+t[i]+
            " format: "+dFmtT.toPattern()+
            "\n\t with source "+s[i]+
            " format: "+dFmtS[i].toPattern()
            );
        System.exit(124);
    } catch (Exception e) {
        System.out.println("Error 122: date_diff(): "+e+
            "\n\t Input row: "+_rowsChecked+" col: "+i+
            " Col-name: "+c[i]+
            "\n\t Comparing target: "+t[i]+
            " format: "+dFmtT.toPattern()+
            "\n\t with source "+s[i]+
            " format: "+dFmtS[i].toPattern()
            );
        System.exit(122);
    } //end catch
} //end function {  date_diff()  }


/**
##########################################################
# Purpose: Compare the two current-index array elements.
# The statements below produce NullPointerException
# if (t[i].equals("") & s[i] == null)    return;
# if (t[i] == null    & s[i].equals("")) return;
# ReferTo: http://forum.java.sun.com/thread.jspa?threadID=791040&messageID=4495425
##########################################################
 *
 * @param i
 */
public static void numeric_diff(int i) {
//String debug = c[i];      // Check which column we are working with here.
try {
    if (isNullDiff(i)) {
        // isNulldiff() caters for null and empty string combinations.
    } else {
        x = new BigDecimal(t[i].trim());       //# First convert to numeric.
        y = new BigDecimal(s[i].trim());       //# First convert to numeric.
        if ( x.compareTo(y) != 0 ) {           //# The two numbers differ.
            if (_tolerance == 0) {             //# Go on to see if an absolute tolerance was specified.
                report_inequality( i );
            } else if (!(in_tolerance(x,y))) { //# Go on to see if tolerance was exceeded.
                report_inequality( i );
            } else {
                // Do nothing.
            } //end if
        } //end if
    } //end if
} catch (Exception e) {
    System.out.println("Warning 110: numeric_diff(): "+e+
        "\n\tComparing: "+t[i]+" & "+s[i]+
        ". Input row: "+_rowsChecked+" col: "+i+
        ". Col-name: "+c[i]+
        ". Using character_diff() instead.");
    character_diff(i);                      //# If the numeric_diff fails do the character_diff.
    return;
}
} //end function {  numeric_diff()  }


/**
##########################################################
# Purpose: Comapre for nulls.
#True if iether target or source values are null.
# The statements below produce NullPointerException
# if (t[i].equals("") & s[i] == null)    return;
# if (t[i] == null    & s[i].equals("")) return;
##########################################################
 *
 * @param i
 * @return
 */
public static boolean isNullDiff(int i) {
    try { //# Cater for null and empty string combinations.
        if (t[i] == null & s[i] == null)    return true;
        if (t[i] != null & s[i] == null) {
            if (t[i].trim().equals("")) {   //# Spaces are considered as null
                return true;                //# Because we consider null and "" equal.
            } else {
                report_inequality( i );
                return true;
            }
        }
        if (t[i] == null & s[i] != null) {
            if (s[i].trim().equals("")) {
                return true;          //# Because we consider null and "" equal.
            } else {
                report_inequality( i );
                return true;
            }
        }
    } catch (Exception e) {
        System.out.println("Error 113: "+e+":\n\t"+t[i]+" & "+s[i]);
        System.exit(113);
    }
return false;
} //end function {  isNullDiff()  }


/**
##########################################################
# Purpose: Count line errors and exit; // if more than allowed,
#          show progress, get another line from the input file and
#          split it into fields.
##########################################################
 *
 * @return
 */
public static String progress_check_and_read() { //function progress_check_and_read()

    try {
        general_progress_check();
        return read_flat_file_line_into_array(); //# Read lines from the flat file while (! _end_of_file.)
    } catch (Exception e) {
        System.out.println( "Error 76: progress_check_and_read(): "+e);
        //e.printStackTrace();
        System.exit(76);
    } finally { }
    return null;
} //end function { progress_check_and_read() }


/**
##########################################################
# Purpose: Drive the main program loop for .SQL input cursor reading.
##########################################################
**/
public static void main_loop_for_sql() { //function main_loop_for_sql()
    try {
        System.out.println(DateUtils.now("HH:mm:ss")+ //"yyyy-MM-dd HH:mm:ss"
                " Reading source database cursor...");
//      i=sourceCursor.getRow();
//      sourceCursor.setFetchSize(1024);
        while (sourceCursor.next()) {                           //# Main read loop for a source query.
            for (int i=1; i<=tgt_col_count; i++) {              //# The key from source. Prefer the empty sting to null.
                s[i] = (sourceCursor.getString(i) != null ? sourceCursor.getString(i) : "") ;
            } //end for

            //# Initialise error indicator for this input row.
            lineHasErr = false;
            _rowsChecked++;

            //# This is particularly important for the troublesome string date format.
            if (_rowsChecked == 1) {
                primeTheSourceColumnDataTypes(); //# Do it once only, because it's a costly exercise.
            }

            //# Jump over the number of input rows specified.
            if (_rowsChecked <= _jump_over_lines) {
                continue; // while
            } //end if

            //# Order the keys to open the cursor with.
            for (int i=1; i<=_key_count; i++) {
                p[i] = s[key_pos[i]];
            } //end for

            //# Record the unique key of every line from the file in a temp table.
            if (reverseCompare) {
                save_key_for_reverse_compare(_key_count);
            } //end if

            //# Do the lookup into the target table.
            if (! open_cursor_with( _key_count )) {  //@TODO Looks like a problem.
                general_progress_check();
                continue; // foreach
            } //end if
            if (fetch_keyed_row(null) == false) {
                report_missing("Missing", null);
                general_progress_check();
                continue; // foreach
            } //end if
            if (_rowsChecked == 1) {
                primeTheTargetColumnDataTypes(); //# Do it once only, because it's a costly exercise.
            }
            //# Compare corresponding SOURCE and TARGET columns.
            int min = (src_field_count >tgt_col_count ? tgt_col_count : src_field_count ); //Get the lowest field count
            for (int i=1; i<=min; i++) {
                if (isKeyColumn[i]) {                  //# Skip over key elements.
                    continue; // for          //Note: Performance can be improved here by implementing a hashmap to columns that need to be audited.
                } //end if
                if (c[i].equals("''") || c[i].equals("\"\"")  ) {
                    continue; // for  //# Skip over null named columns. # http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html
                } //end if
                _nonKeyColsAudited++;
                switch (_comparisonType[i]) {
                        case _numeric   : numeric_diff(i); break;
                        case _date      : date_diff(i );   break;
                        case _character : clever_diff(i);  break;
                } // end switch
            } //end for
            general_progress_check();

        } //end while //############### END of SQL Main Program Loop ###########
    } catch (SQLException e) {
        System.out.println(
            "Error 121: main_loop_for_sql(): SQLException ErrCode: "
            +e.getErrorCode()+": "+e);
        //e.printStackTrace();
    } catch (Exception e) {
        System.out.println( "Error 77: main_loop_for_sql(): "+e);
        //e.printStackTrace();
    } finally {}
} //end function { main_loop_for_sql() }


/**
##########################################################
# Purpose: Just a place to keep this unweildy peice of 4GL code.
#          Array t used to minimize the mess.
##########################################################
 *
 * @param inputLine
 * @return
 */
public static boolean fetch_keyed_row(String inputLine) {
    int i;
    try { //whenever error do_nothing;
        if (targetCursor.next() == false) {
            return false;
        }
        for (i=1; i<=tgt_col_count; i++) {
            t[i] = targetCursor.getString(i); //# Fetch the target Cursor into the t array.
        } //end for
    } catch (SQLException e) {
        System.out.println(
            "Warning 4: SQLException ErrCode: SQLException ErrCode: "
            +e.getErrorCode()+": "+e+
            " Database fetch of input line no: "+ _rowsChecked
            );
        if (inputLine == null) {
            inputLine = "" ;
            for (i=1; i<=tgt_col_count; i++) {
                inputLine += s[i]+"|";       //# Reassemble the input line.
            } //end for
        } //end if
        System.out.println("Bad line: "+inputLine);
        //throw e;
        return false;
    } catch (Exception e) {
        System.out.println( "Error 78: fetch_keyed_row(): "+e);
        //e.printStackTrace();
        System.exit(78);
    } finally { } //whenever error stop
    return true;

} //end function {  fetch_keyed_row()  }


/**
##########################################################
# Purpose: Report the missing rows from TARGET.
##########################################################
 *
 * @param aReason
 * @param f
 */
public static void report_missing(String aReason ,SQLException f ) {
    lineHasErr = true;                          //# Set the indicator.
    String sheetLine ="";
    for (int i=1; i<=_key_count; i++) {         //# The key from source.
        sheetLine +=  s[key_pos[i]]+ "\t";
    } //end for
    try {
        spreadSheet.println(
            sheetLine.trim()+"\t"+aReason.trim()+" "+f+"\t\t\t"+_rowsChecked);
    } catch (Exception e) {
        System.out.println( "Error 300: report_missing(): "+e);
        //e.printStackTrace();
        System.exit(300);
    }
    max_err_exit();
} //end function {  report_missing()  }


/**
##########################################################
# Purpose: Optionally treat text codes as numbers rather
#     than as strings in order to determine a difference.
// Even if it't character data, check if it is numeric.
##########################################################
 *
 * @param i
 */
public static void clever_diff(int i) { //function clever_diff()
    try {
        if (numericStringsOn) {
            if        ( ! is_numeric( t[i] )) { character_diff(i); return;
            } else if ( ! is_numeric( s[i] )) { character_diff(i); return;
            } else if ( is_date(      t[i] )) {
                   if ( is_date(      s[i] )) { date_diff(     i); return; }
            } else if ( is_date(      s[i] )) { character_diff(i); return;
            } else {                            numeric_diff(  i); return;
            } //end if
        } else {
            character_diff(i);
        } //end if
/*      This code was replaced with above, to exit early if possible.
        if (numericStringsOn) {
            if  ( is_numeric( t[i] )
            & (! is_date( t[i] ))     // Notice the use of "&" and not "&&"
            &  is_numeric( s[i] )    // because all the tests must pass.
            & (! is_date( s[i] ))
            ) {
                numeric_diff(i);
            } else {
                character_diff(i);
            } //end if
        } else {
            character_diff(i);
        } //end if
*/
 }catch (Exception e) {
        System.out.println("Error 111: clever_diff():"+e+" Source: "+s[i]+" Target: "+t[i]);
        System.exit(111);
    }
} //end function { clever_diff() }


/**
##########################################################
# Purpose: Determine if a string is numeric.
# ranges covering the ascii set.
# Returns: False if [certain text ranges covering the
 *         ascii set or spaces are matched] else true.
# Bounds : Valid characters in a numeric are "[0-9.e-]" and
#          anything else is not numeric.
# Author : MB
# Date   : Thu Sep 20 09:47:29 USAST 2007
##########################################################
 *
 * @param s
 * @return
 */
public static boolean is_numeric( String s ) { //function is_numeric( t )
    try {
        if ( s == null ||
            s.trim().equals("") ||
//          s.trim().matches( ".*[!-\"/*;-d:f-~() \t-].*" ) ) {
            s.trim().matches( ".*[!-\"/*;-d:f-~() \t].*" ) ) {
            return false;
        } //end if
    } catch (Exception e) {
        System.out.println("Error 88: "+e+": "+s);
        System.exit(88);
    }
    return true;
} //end function {  is_numeric( c )


/**
##########################################################
# Purpose: Report the mismatching column with its key
 * by building up a tab delimited string and writing it
 * into the spreadsheet.
##########################################################
 *
 * @param c_
 */
public static void report_inequality( int c_ ) { //function report_inequality( c_ )
    int i;
    String sheetLine="";
    lineHasErr = true;                       //# Set the indicator for lines/rows with errors.
    _columnsWithErrors++;
    try {
        for (i=1; i<=_key_count; i++) {          //# The key from source.
            sheetLine += p[i].trim()+"\t";
        } //end for
        sheetLine +=
            c[c_].trim()                  +"\t"+ //# Column name.
            see_a_null( t[c_] )           +"\t"+ //# Value from TARGET.
            see_a_null( s[c_] )           +"\t"+ //# Value from Source.
            _rowsChecked                  +"\t"+ //# Line No from Source File.
            c_                            +"\t"+ //# Column No from Source File.
            see_a_null( _srcMetaType[c_]) +"\t"+ //# Meta Data type.
            see_a_null( _srcMetaCol[c_])  +"\t"+ //# Meta Data column.
            see_a_null( _srcMetaTab[c_]);        //# Meta Data table.
        if (_comparisonType[c_]==_date) {
            sheetLine += "\t"+
            (dFmtT==null ? "Null" : dFmtT.toPattern())+"\t"+   //# Target date format.
            (dFmtS[c_]==null ? "Null" : dFmtS[c_].toPattern());//# Source date format.
        }
        spreadSheet.println( sheetLine );
    } catch (Exception e) {
        System.out.println( "Error 79: report_inequality(): "+e);
        //e.printStackTrace();
    } finally {}
    max_err_exit();
} //end function {  report_inequality()  }


/**
##########################################################
# Purpose: Calulate if the numeric tolerance has been exeeded.
##########################################################
 *
 * @param x
 * @param y
 * @return
 */
public static boolean in_tolerance(BigDecimal x, BigDecimal y) {
    if (percentOn) {                     //# -v on command line
        if (perc_diff() > _tolerance) {
            return false;
        } //end if
    } else {
        if (x.abs().add(y.abs().negate()).floatValue() > _tolerance) { //.4GL: if abs_(ch_null(t)-ch_null(t)) > _tolerance then
            return false;
        } //end if
    } //end if
    return true;

} //end function { in_tolerance() }


/**
############################################################
# Purpose: Determine the percentage difference between <x>,<y>
# Returns: the percentage, null if either of <x>, <y> are null
# Bounds :
# See    :
# Authors: Lyneve Lesch
############################################################
 *
 * @return
 */
public static float perc_diff() { //function perc_diff( )

    if (y.equals(0) && x.equals(0)) {
        return 0;
    } //end if
    if (!(x.equals(0))) {
        return x.add(y.negate()).divide(x, 6, 4).abs().movePointRight(2).floatValue(); //# http://www.jroller.com/nwinkler/entry/the_trouble_with_bigdecimal
    } //end if
    return 0;

} //end function {  perc_diff()  }


/**
##########################################################
# Purpose: Count line errors, exit if more than allowed and
#          show progress.
##########################################################
**/
public static void general_progress_check() { //function general_progress_check()
    if (lineHasErr) {
        _rowsWithErrors++; //# Count lines with errors.;
    } //end if
    if (_rowsWithErrors >= _max_line_err) {
        try {
            spreadSheet.println( log_progress().trim() );
            String msg = "Maximum number of lines with errors reached: "+
                _rowsWithErrors;
            System.out.println(msg);
            spreadSheet.println( msg.trim() );
        } catch (Exception e) {
            System.out.println( "Error 301: general_progress_check(): "+e);
            //e.printStackTrace();
        }
        end_main();
        System.exit(0); // normal exit.
    } //end if
    if (( _rowsChecked % _progress_every == 0) || (_rowsChecked == 1 )) {
        String msg = log_progress();
    } //end if
} //end function { general_progress_check() }


/**
##########################################################
# Purpose: Open the target cursor and Trap array boundary violations.
# Retruns: True if a row was fetched.
##########################################################
 *
 * @param keys_ the number of columns that make up the key.
 * @return
 */
public static boolean open_cursor_with(int keys_) { //function open_cursor_with(keys_)
    int i=0;
    try { //whenever error do_nothing;
        if (keys_ > maxKeys) {
            System.out.println("Error 3: Recompile this program"+
            " to take "+keys_+ " columns in the maxKeys variable.");
            System.exit(3);
        } //end if
        for (i=1;i<=keys_;i++) {                        //# NB See comment elsewhere in the code for an opportunity to improve performance.
            targetQueryPreped.setString(i,p[i].trim());
        } //end for
        targetCursor = targetQueryPreped.executeQuery();
    } catch (SQLException e) {
        report_missing(e.getMessage(),e);
        if (!(suppressWarnings)) {
            System.out.println(
                "\nWarning 5: Target open_cursor_with('"+keys_+
                (keys_== 1 ? " key" : " keys" )+"'): SQLException ErrCode: "
                +e.getErrorCode()+": "+e+
                "\n       This error happens while trying to look up a target row."+
                "\n       Hint 1: Run the target query from the \""+targetTable+
                    ".meta_qa.sql\" file substituting the \"?\" for the keys below."+
                "\n       You may have to modify the source format of dates in the key."+
                "\n       Hint 2: You can Google this error message for help..."+
                "\n       Hint 3: You can suppress this message with the -w option."+
                "\n Target Table: "+ targetTable.trim()+
                "\n   Input File: "+ inputFileName.trim()+
                "\n    Record No: "+ _rowsChecked
                );
            String msg ="";                                 //# Show keys.
            for (i=1; i<=_key_count; i++) {
                msg += c[i].trim()+" ";
            } //end for
            System.out.println("      Key Col: "+msg.trim());
            msg="";                                         //# Show Values.
            for (i=1; i<=_key_count; i++) {
                msg += p[i]+" ";
            } //end for
            System.out.println("      Key Val: "+msg.trim());
            msg="";                                         //# Show Data.
            for (i=1;i<=src_field_count;i++) {
                msg = msg + (s[i] == null ? "" : s[i].trim() )+"|";
            } // end for
            System.out.println("         Data: "+msg.trim());
        }
        max_err_exit();
        return false;
    } catch (Exception e) {
        System.out.println( "Error 80: open_cursor_with('"+keys_+" keys'): "+e);
        //e.printStackTrace();
        System.exit(80);
    } finally { }
    return true;
} //end function {  open_cursor_with(keys_)  }

/**
##########################################################
# Purpose: Records the input key for reverse comarion at } //end of job.
#    Note: If the 20 key element limitation is changed, do it here too.
##########################################################
 *
 * @param keys_
 */
public static void save_key_for_reverse_compare(int keys_) { //function save_key_for_reverse_compare(keys_)
    if (keys_ > maxKeys) {
        System.out.println("Error 30: Recompile this program"+
        " to take "+keys_+ " columns in the maxKeys variable.");
        System.exit(30); //exit program 30
    } //end if
    try { //whenever error do_nothing;
        //# Max keys per table found in meta-data was 13 at the time of writing this program.
        for (int i=1; i<=_key_count; i++) {
            reverseKeyUpd.setObject(i,p[i]);
        } //end for
        reverseKeyUpd.executeUpdate();
    } catch (SQLException e) {
        System.out.println(
            "Error 0: Inserting reverse check key: SQLException ErrCode: "
            +e.getErrorCode()+": "+e);
        System.out.println("Description: "+e);
        System.out.println("      Table: "+ targetTable);
        System.out.println("    In File: "+ inputFileName);
        System.out.println("    Line No: "+ _rowsChecked);

        String KeyColsStr ="";
        String KeyValsStr ="";
        String DataStr ="";
        for (int i=1; i<=_key_count; i++) {
            KeyColsStr= _scratch.trim()+" "+ c[i];
            KeyValsStr= " "+ p[i];
            DataStr= " "+ s[i];          // This is the source data.
        } //end for
        System.out.println("    Key Col: "+KeyColsStr);
        System.out.println("    Key Val: "+KeyValsStr);
        System.out.println("       Data: "+DataStr);

    } catch (Exception e) {
        System.out.println( "Error 81: save_key_for_reverse_compare(): "+e);
        //e.printStackTrace();
        System.exit(81);
    } finally {
         max_err_exit();
    } //whenever error stop
} //end function { save_key_for_reverse_compare() }

/**
##########################################################
# Purpose: Exit if maximum errors reached.
##########################################################
**/
public static void max_err_exit() { //function max_err_exit()
int m = (_rowsWithErrors + _columnsWithErrors + _missingRowsInReverseCompare);
    if (m >= _max_err) {
        String msg= "Maximum errors parameter was reached: "+ m;
        System.out.println( msg );
        try {
            spreadSheet.println( msg );
        } catch (Exception e) {
             System.out.println( "Error 302: max_err_exit(): "+e);
             //e.printStackTrace();
             System.exit(302);
        }
        end_main();
        System.exit(0); //exit; // program 0
    } //end if
} //end function { max_err_exit() }

/**
##########################################################
# Purpose: Standardise the end of run from early exits.
##########################################################
**/
public static void end_main() { //function end_main()
    int i;
    String txt = null;
    try {
        spreadSheet.println( "End of data." );
        spreadSheet.println( "Statistics of the run:" );
        spreadSheet.println( log_progress().trim() );
        txt= DateUtils.now("HH:mm:ss")+ //"yyyy-MM-dd HH:mm:ss"
                " Program ended normally checking table:  "+ targetTable.trim();
        System.out.println(txt);
        spreadSheet.println(txt);
        txt = null;

        spreadSheet.println( "Columns audited in this run: " ); //# Show columns audited and not audited in this run.
        _scratch=
            "select field_name "+
            " from meta_dwh_table_field "+
            " where lower(table_name) = lower('"+ targetTable.trim()+"')"+
            "   and lower(source_system_name) like lower('*"+sourceSystem.trim()+"*')"+
            "   and lower(field_name) not in ('";
        String inList = "";
        for (i=1; i<=tgt_col_count; i++) {
            _scratch= _scratch.trim()+c[i].trim().toLowerCase()+"','";
            inList = inList+ c[i].trim().toLowerCase()+ ", ";
        } //end for
        _scratch = _scratch.replaceAll(",'$","")+ ")"+ " order by design_sequence;";
        spreadSheet.println( inList.replaceAll(", $",".") ); //relace final comma with "."
        //# Incase this table is not in this database...;
        metaColsNotAuditedPrep = metaJdbcConn.prepareStatement(_scratch);
        ResultSet not_meta_c = metaColsNotAuditedPrep.executeQuery();
        _scratch="";
        while (not_meta_c.next()) { //Loop through the cursor.
            targetColumn = not_meta_c.getString(1);
            _scratch+=targetColumn+", ";
        } //end while
        not_meta_c.close();
        spreadSheet.println( "Columns in meta data not audited in this run: \n"+
            _scratch.trim().replaceAll(", $", ".") );

        _scratch= "Program: "+ _version.trim()+
            " audit trail. End of run date: "+
            //DateUtils.now("yyyy-MM-dd HH:mm:ss");   //# Changed deliberately to differentiate $GL logs entries from Java entries.
            DateUtils.now("dd MMM yyyy HH:mm:ss");//# Mon Aug  4 2008 @
        System.out.println(_scratch.trim());
        spreadSheet.println( _scratch.trim() );
        spreadSheet.flush();
        spreadSheet.close(); // The file should be released here, but is not always the case.
        File auditLogFile = new File( "meta_qa.log" );
        if (!(auditLogFile.exists())) { // http://www.rgagnon.com/javadetails/java-0070.html
            logFile = new PrintWriter(
                      new FileWriter( auditLogFile,false ),true); // Append.
            logFile.println(
                "Start time"+          "\t"+
                "End time"+            "\t"+
                "Table name"+          "\t"+
                "Input file name"+     "\t"+
                "Rows checked"+        "\t"+
                "Columns checked"+     "\t"+
                "Rows with errors"+    "\t"+
                "Columns with errors"+ "\t"+
                "Reverse missing rows"+"\t"+
                "User Id"+             "\t"+
                "Interface number"+    "\t"+
                "AIT Xaction number" );
        } else {
            logFile = new PrintWriter(
                      new FileWriter( auditLogFile,true ),true); // Over write.
        } // end if
        endTime = DateUtils.now("yyyy-MM-dd HH:mm:ss"); // Play with star and end time class definition and initialization.
        logFile.println(
            startTime+                   "\t"+
            endTime.trim()+              "\t"+
            targetTable.trim()+          "\t"+
            inputFileName.trim()+        "\t"+
            _rowsChecked+                "\t"+ //# Rows checked.
            _nonKeyColsAudited+          "\t"+ //# Columns checked.
            _rowsWithErrors+             "\t"+ //# Rows with errors.
            _columnsWithErrors+          "\t"+ //# Columns with errors.
            _missingRowsInReverseCompare+"\t"+ //# Reverse missing rows.
            userId.trim()+               "\t"+ //# The unix user id.
            interfaceCode+               "\t"+ //# The unique Meta Data Interface Number.
            transactionType.trim()             //# The unique AIT transaction type.
            );
        logFile.close();
    } catch (SQLException e) {
        System.out.println( "Error 447: end_main(): SQLException ErrCode: "
            +e.getErrorCode()+": "+e);
        System.exit(447);
    } catch (IOException e) {
        System.out.println( "Error 448: end_main(): "+e);
        System.exit(448);
    } catch (Exception e) {
        System.out.println( "Error 449: end_main(): "+e);
        e.printStackTrace();
        System.exit(449);
    } finally { } //whenever error stop
} //end function { end_main() }


/**
##########################################################
# Purpose: Log elapsed time.
##########################################################
 *
 * @return
 */
public static String log_progress() { //function log_progress()
     String msg =
         DateUtils.now("HH:mm:ss")+ //"yyyy-MM-dd HH:mm:ss"
         " Rows checked: "+        _rowsChecked+
         ", Columns checked: "+     _nonKeyColsAudited+
         ", Rows with errors: "+    _rowsWithErrors+
         ", Columns with errors: "+  _columnsWithErrors+
         ", Total of all errors: "+
            (_rowsWithErrors+_columnsWithErrors+_missingRowsInReverseCompare);
     if (reverseCompare) {
         msg += ", Reverse-compare loss: "+_missingRowsInReverseCompare;
     } //end if
     System.out.println(msg.trim());
     return msg;
} //end function {  log_progress()  }


/**
 * ###################################################################
 * # Purpose: Open input and output files;
 * #          database connections;
 * #          and prepare SQL.
 * ###################################################################

 * Notes:
 * Before you test with an app, ensure the JDBC driver returns
 * version information by running this command on the command line:
 * java oracle.jdbc.driver.OracleDriver
 * If that command does not work, you still have environment problems and
 * need to revisit before you test with the app.

 * Database Connection Notes:
 * http://java.sun.com/docs/books/tutorial/jdbc/basics/connecting.html
 * To register the Java DB driver, add the following line of code:
 * Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
 * http://www.devdaily.com/java/edu/pj/pj010024/
 //**  AIX and INFORMIX specific conficuration stuff.
     * Look on the AIX box. Sockets have names in the $INFORMIXDIR/etc/services file.
     * dwh@dwhdv:/dwhtest/user $ grep -i informix /etc/services | grep -i Socket
     *   onlsoc 1526/tcp  # INFORMIX Socket connection
     * The INFORMIXSERVER is defined in the $INFORMIXDIR/etc/sqlhosts file.
     * dwh@dwhdv:/usr/informix/etc $ grep 'onlsoc$' $INFORMIXDIR/etc/sqlhosts
     *   dwhdv_soc  onsoctcp  dwhdv  onlsoc
* dwh@dwh:  Production.
     * The services file in production was found with this: dwh@dwh:/usr $ find . -name services -print 2>/dev/null
     * In: ./informix/dbadmin/logs/services
     * And contained this:
sqlexec         1525/tcp        # INFORMIX SQL
onlsoc          1526/tcp        # INFORMIX Socket connection
onlshm          1527/tcp        # Informix shared memory
sqlexec1        1528/tcp        # INFORMIX SQL1
onlsoc1         1529/tcp        # INFORMIX socket
coauthor        1529/tcp        # oracle
coauthor        1529/udp        # oracle
     * To find the IP address type ping hostname. e.g. ping dwh.
 * Hardcoded JDBC stuff that was removed in version 21.0
        url="jdbc:oracle:thin:@hostname:1521:orcl"; //Oracle example: http://www.jguru.com/faq/view.jsp?EID=444466
        url = "dwhdev.dwhdbdev.woolworths.co.za";   // Not sure what this is. Provided by Sean Poulter.
        url = "jdbc:oracle:thin:@dwhdbdev:1521:dwhdev"; // Oracle dwhdbdev machine.
        // url = "jdbc:informix-sqli://129.100.1.175:1526/dwh:INFORMIXSERVER=dwh_soc"; //host: dwh  Informix
        url = "jdbc:informix-sqli://10.36.157.76:1529/dwh:INFORMIXSERVER=dwhdv_soc"; //host: dwhdv Informix
        url = "jdbc:informix-sqli://129.100.1.175:1529/dwh:INFORMIXSERVER=onlsoc1"; //host: dwh  Informix
        //targetJdbcDriver = "com.oracle.jdbc.OracleDriver"; // Oracle JDBC Driver.
        //targetJdbcDriver = "com.informix.jdbc.IfxDriver";  // Informix JDBC Driver.
 *
 * @throws java.io.IOException
 */
public static void prepare_meta_qa() throws IOException {
    System.out.println(DateUtils.now("yyyy-MM-dd HH:mm:ss")+
            " Now auditing table: " + targetTable);
    try { //# Make the Meta-data DB connection.
        System.out.println(DateUtils.now("HH:mm:ss")+
                " Loading meta-data JDBC driver: "+metaJdbcDriver);
        Class.forName(metaJdbcDriver);                         //# Load the meta-data JDBC Driver.
        System.out.println(DateUtils.now("HH:mm:ss")+
                " Using JDBC url: "+metaJdbcUrl+
                "\n\t and Connecting as user: "+
                metaJdbcUserId);
        metaJdbcConn = DriverManager.getConnection(            //# Make the connection to the database.
                metaJdbcUrl, metaJdbcUserId, metaJdbcPassword);
        if (metaJdbcDriver.toLowerCase().matches(".*oracle.*")) {
            metaJdbcConn.setTransactionIsolation(               //# http://www.orafaq.com/node/37
                metaJdbcConn.TRANSACTION_READ_COMMITTED);
        } else {
            metaJdbcConn.setTransactionIsolation(               //# http://www.orafaq.com/node/37
                metaJdbcConn.TRANSACTION_READ_UNCOMMITTED);
        }
    } catch (ClassNotFoundException e) {
        System.out.println("Error 341: prepare_meta_qa(): "+e);
    } catch (ExceptionInInitializerError e) {
        System.out.println("Error 342: prepare_meta_qa(): "+e);
    } catch (LinkageError e) {
        System.out.println("Error 344: prepare_meta_qa(): "+e);
    } catch (SQLException e) {
        System.out.println("Error 345: prepare_meta_qa(): SQLException ErrCode: "
            +e.getErrorCode()+": "+e);
        System.exit(345);
    } catch (Exception e) {
        System.out.println("Error 346: prepare_meta_qa(): "+e);
        System.exit(346);
    } finally { } //end finally

    try { //# Make the target DB connection.
        if (   targetJdbcDriver  .equals(metaJdbcDriver)
            && targetJdbcUrl     .equals(metaJdbcUrl)
            && targetJdbcUserId  .equals(metaJdbcUserId)
            )
        {
            System.out.println(DateUtils.now("HH:mm:ss")+
                " Reusing the meta-data connection for target data.");
            targetJdbcConn = metaJdbcConn;
        } else {
            System.out.println(DateUtils.now("HH:mm:ss")+
                " Loading target JDBC driver: "+targetJdbcDriver);
            Class.forName(targetJdbcDriver);                    //# Load the  JDBC Driver.
            System.out.println(DateUtils.now("HH:mm:ss")+
                " Using JDBC url: "+targetJdbcUrl+
                "\n\t and Connecting as user: "+
                targetJdbcUserId);
            targetJdbcConn = DriverManager.getConnection(       //# Make the connection to the database.
                targetJdbcUrl, targetJdbcUserId,
                targetJdbcPassword);
        }
        if (targetJdbcDriver.toLowerCase().matches(".*oracle.*")) {
            targetJdbcConn.setTransactionIsolation(             //# http://www.orafaq.com/node/37
                targetJdbcConn.TRANSACTION_READ_COMMITTED);
// Set the target DBdate format.  Unfortunately the Oracle JDBC driver seems to ignote this statement. Pity!
//            if (dFmtT != null) {
//                processSqlStatement(
//                    ("alter session set NLS_DATE_FORMAT='"
//                    +dFmtT.toPattern()+"'") ,targetJdbcConn );
//            }
        } else {
            targetJdbcConn.setTransactionIsolation(             //# http://www.orafaq.com/node/37
                targetJdbcConn.TRANSACTION_READ_UNCOMMITTED);
        }
    } catch (ClassNotFoundException e) {
        System.out.println("Error 311: prepare_meta_qa(): "+e);
    } catch (ExceptionInInitializerError e) {
        System.out.println("Error 312: prepare_meta_qa(): "+e);
    } catch (LinkageError e) {
        System.out.println("Error 314: prepare_meta_qa(): "+e);
    } catch (SQLException e) {
        System.out.println("Error 315: prepare_meta_qa(): SQLException ErrCode: "
            +e.getErrorCode()+": "+e);
        System.exit(315);
    } catch (Exception e) {
        System.out.println( "Error 316: prepare_meta_qa(): "+e);
        e.printStackTrace();
        System.exit(316);
    } finally { } //end finally

    if (!(tempTargetTabSqlFile.equals("") || tempTargetTabSqlFile == null)) {
        runTargetSqlSetupFile(tempTargetTabSqlFile);            //# Make a temporary target table.
    } //end if

    //# Is the input file extension '.sql'?
    String targetQuery = null;
    if (inputFileName.toLowerCase().endsWith(".sql")) {
        sqlInput = true;
        targetQuery = inputIsSourceDbaseSql(); // Ie data from a source database Query.
    } else {
        targetQuery = inputIsLocalFlatFile();  // Ie data from a local delimited flat file.
    } //end if

    //# Record the generated target Query in the .sql file.
    try {
        generatedTargetSqlFile =
            new PrintWriter(
            new FileWriter(targetTable + ".meta_qa.sql"),true); //# Over write.
        generatedTargetSqlFile.println(targetQuery);            //# Write the query.
        generatedTargetSqlFile.close();                         //# close the file.
    } catch (Exception e) {
        System.out.println(
            "Error 85: Writing file: \""+targetTable+".meta_qa.sql\" "+e);
        e.printStackTrace();
        System.exit(85);
    }
    try { //# Prepare the SQL for the lookup into the target table.
        targetQueryPreped =  targetJdbcConn.prepareStatement(targetQuery);
    } catch (SQLException e) {
        System.out.println(
            "Warning 86: prepare_meta_qa(): SQLException ErrCode: "
            +e.getErrorCode()+": "+e+": '"+targetQuery+"'");
    } catch (Exception e) {
        System.out.println( "Error 106: prepare_meta_qa(): "+e);
        e.printStackTrace();
        System.exit(106);
    } finally { }
    if (reverseCompare) {
        build_the_reverse_query();
    } //end if

    //# Open the output file and write a header.
    spreadSheet = new PrintWriter(
                  new FileWriter( targetTable+".xls",false ),true); // Over write.
    spreadSheet.println(
        "Program: "        +_version.trim()      +" "+
        "Audit of table: " +targetTable.trim()   +" "+
        "compared to: "    +inputFileName.trim() +" "+
        "Run date: "       +new Date().toString()
        );
    if (numericStringsOn) {
        spreadSheet.println(
            "Numeric expressions in text were converted. (eg. 0001.00 = 1)" );
    } else {
        spreadSheet.println(
            "Character data was treated literally. (eg. \"1\" != \"0001\")" );
    } //end if

    //# Output the spreadSheet audit trail heading line.
    String msg ="";
    for (int i=1; i<=_key_count; i++) {
        if (key_pos[i] == 0) {
            System.out.println(
                "Error 18: A source data column that could not"
                +"\n\t be found in the target table is in the key"
                +"\n\t required for the lookup. See column: "+ i);
            System.exit(18); //exit program 18
        } //end if
        msg += c[key_pos[i]]+"\t";
    } //end for
    msg +=
        "Col Name"+              "\t"+
        targetDBase.trim()+ " Value"+ "\t"+
        "Source Value"+          "\t"+
        "In Line#"+              "\t"+
        "In Col#"+               "\t"+
        "Data Type"+             "\t"+
        "Column"+                "\t"+
        "Table"+                 "\t"+
        "Tgt DateFmt"+           "\t"+
        "Src DateFmt";
    spreadSheet.println( msg.trim() );

} //end function { prepare_meta_qa() }



/**
##########################################################
# Purpose: Build the following SQLs where A,B,C is the unique key.
#         ##Create the temp table.
#          select first 1 A,B,C from target_table into temp temp_source_key_table;
#         ##Empty the temp table.
#          delete from temp_source_key_table where 1=1;
#         ##Insert the source keys into the temp table.
#          insert into temp_source_key_table values (?,?,?);
#         ##Do the reverse comparison.
#          select A,B,C from target_table A where (! exists)
#          (select A,B,C from temp_source_key_table B
#               where A.A=B.A and A.B=B.B and A.C=B.C ...);
#   Usage: if (is_param( "-r" )) {  build_the_reverse_query() } //end if;
##########################################################
**/
public static void build_the_reverse_query() { //function build_the_reverse_query()
    String key_string = "",            //# Contains the A,B,C...N column names.
        key_places = "",               //# Contains the ?,?,?...? column value place holders.
        key_join = "";                 //# Contains where A.A=B.A and A.B=B.B and A.C=B.C ...).
    int i = 0;
    try {
        //# Add the SQL join using the key column names.
        for (i=1; i<=_key_count; i++) {
            key_string += c[key_pos[i]]+ ",";
            key_places += "?,";
            key_join += " A."+c[key_pos[i]]+
                        "=B."+c[key_pos[i]]+ " and";
        } //end for
        //# Remove trailing delimiters.
        key_string = key_string.replaceAll(",$","");  //# Remove the last "," comma.;
        key_places = key_places.replaceAll(",$","");  //# Remove the last "," comma.;
        key_join   = key_join.replaceAll(" and$",""); //# Remove the last "and".;

        //# Create the temp table.
        processSqlStatement(
            "select "+ key_string+ " from "+ targetTable+
            " where 1=0 into temp temp_source_key_table with no log",
            targetJdbcConn);

        //# Create the temp index.
        processSqlStatement(
                "create index temp_index on temp_source_key_table ("
                + key_string.trim()+ ")",
                targetJdbcConn
            );

        //# Create insert statement.
        reverseKeyUpd = targetJdbcConn.prepareStatement(
            "insert into temp_source_key_table values ("
            + key_places + ")"
        );

        //# Create reverse compare.
        reversePrepedQuery = targetJdbcConn.prepareStatement(
            "select "+key_string+ " from "+ targetTable +
            " A where not exists (select "+ key_string +
            " from temp_source_key_table B where "+ key_join + ")"
        );  //# A cursor "reverseCursor", will be declared for reversePrepedQuery.

    } catch (SQLException e) {
        System.out.println( "Error 107: build_the_reverse_query(): "
            +"SQLException ErrCode: "+e.getErrorCode()+": "+e);
        //e.printStackTrace();
    } catch (Exception e) {
        System.out.println( "Error 87: build_the_reverse_query(): "+e);
        //e.printStackTrace();
    } finally { }

} //end function {  build_the_reverse_query() }


/**
##########################################################
# Purpose: Load the source Query into memory;
#          strip out any comments;
#          Run all the statements and
#          prepare a cursor on the last one.
##########################################################
 * Java Notes:
 *  sourceCursor is a standard JDBC ResultSet. It maintains a
 *  cursor that points to the current row of data. The cursor
 *  moves down one row each time the method next() is called.
 *  You can scroll one way only--forward--with the next()
 *  method. When auto-commit is on, after you reach the
 *  last row the statement is considered completed
 *  and the transaction is committed.
 * // http://java.sun.com/docs/books/tutorial/essential/io/examples/FileStuff.java
 * //JDBC cursor example.
 * //http://publib.boulder.ibm.com/infocenter/cscv/v10r1/index.jsp?topic=/com.ibm.cloudscape.doc/cdevconcepts41275.html
 *
 * @return
 */
public static String inputIsSourceDbaseSql() {
    try { //# Make the source DB connection.
        if (   sourceJdbcDriver  .equals(metaJdbcDriver)
            && sourceJdbcUrl     .equals(metaJdbcUrl)
            && sourceJdbcUserId  .equals(metaJdbcUserId)
            )
        {
            System.out.println(DateUtils.now("HH:mm:ss")+
                " Reusing the meta-data connection for source data.");
            sourceJdbcConn = metaJdbcConn;
        } else if (
               sourceJdbcDriver  .equals(targetJdbcDriver)
            && sourceJdbcUrl     .equals(targetJdbcUrl)
            && sourceJdbcUserId  .equals(targetJdbcUserId)
            )
        {
            System.out.println(DateUtils.now("HH:mm:ss")+
                " Reusing the target data connection for source data.");
            sourceJdbcConn = targetJdbcConn;
        } else {
            System.out.println(DateUtils.now("HH:mm:ss")+
                " Loading source JDBC driver: "+sourceJdbcDriver);
            Class.forName(sourceJdbcDriver);                        //# Load the  JDBC Driver.
            System.out.println(DateUtils.now("HH:mm:ss")+
                " Using JDBC url: "+sourceJdbcUrl+
                "\n\t and Connecting as user: "+
                sourceJdbcUserId);
            sourceJdbcConn = DriverManager.getConnection(           //# Make the connection to the database.
                sourceJdbcUrl, sourceJdbcUserId, sourceJdbcPassword);
        }
        if (sourceJdbcDriver.toLowerCase().matches(".*oracle.*")) {
            sourceJdbcConn.setTransactionIsolation(                //# http://www.orafaq.com/node/37
                sourceJdbcConn.TRANSACTION_READ_COMMITTED);
        } else {
            sourceJdbcConn.setTransactionIsolation(                //# http://www.orafaq.com/node/37
                sourceJdbcConn.TRANSACTION_READ_UNCOMMITTED);
        }
    } catch (ClassNotFoundException e) {
        System.out.println("Warning 331: inputIsSourceDbaseSql(): "+e);
    } catch (ExceptionInInitializerError e) {
        System.out.println("Warning 332: inputIsSourceDbaseSql(): "+e);
    } catch (LinkageError e) {
        System.out.println("Warning 334: inputIsSourceDbaseSql(): "+e);
    } catch (SQLException e) {
        System.out.println("Error 335: inputIsSourceDbaseSql(): "
            +"SQLException ErrCode: "+e.getErrorCode()+": "+e);
        System.exit(335);
    } catch (Exception e) {
        System.out.println("Error 336: inputIsSourceDbaseSql(): "+e);
        e.printStackTrace();
        System.exit(336);
    } finally { } //end finally

    String query[] = null;
    int i = 0;
    String inputSql = "", inputLine = "";
    try {
        System.out.println(DateUtils.now("HH:mm:ss")+
            " Processing the source SQL file: "+inputFileName);
        sqlInputFile = new BufferedReader(
                       new InputStreamReader(
                       new FileInputStream(inputFileName)));         //# Open inputFileName.
        do { //while                                                 //# loop Through the file.
            inputSql += " "+inputLine.replaceFirst("--.*","").trim();//# Keep everything before a comment and toss the rest.
            inputLine =  sqlInputFile.readLine();
        } while (inputLine != null) ; //end while loop
        query = inputSql.replaceAll(";[ \t]*$","").split(";",0);    //# Remove trailing semi-colun & white-space and then Separate the queries on semicolons.
        int lastQueryNo = query.length -1;
        for (i=0;i<lastQueryNo;i++) {                               //# Execute all but the last targetQuery.
            System.out.println(DateUtils.now("HH:mm:ss")+
                " Running query #"+i+": "+query[i]);
            processSqlStatement(query[i],sourceJdbcConn);           //# NB. no semicolons to follows JDBC statements.
        }
        System.out.println(DateUtils.now("HH:mm:ss")+
            " Source cursor on query, #"+(i+1)+":\n\t "+query[i].trim());
        //# There should be at least one AS Column-Synonym clause in the select.
        if (query[i].toLowerCase()
                    .replaceAll("[ \t]from[ \t].*$","")                         //# Remove the tables, because they also may contain " AS SYNONYM" clauses.
                    .matches("^.*[ \t]as[ \t].*$")) {
        } else {
            System.out.println(
                "Error 140: No 'AS Target-Column-Synonym' clause found in select."
                );
            System.exit(140);
        }
        sourceCursor = sourceJdbcConn.createStatement().executeQuery(query[i]); // Statement sourceQueryPreped = sourceJdbcConn.createStatement(); //# Prepare source_sql_p from sql_. // sourceCursor = sourceQueryPreped.executeQuery(query[i]);   //# The last one is for the cursor.

        //# Get target column names from one of: the last SQL "as" clauses; or from meta-data.
        System.out.println(DateUtils.now("HH:mm:ss")+
            " Deriving the query for the target table based on the input SQL.");
        targetTableQuery = build_input_sql_based_target_sql(query[i]);
        if (targetTableQuery == null) {
            System.out.println(DateUtils.now("HH:mm:ss")+
                "Resorting to meta-data to build the target table query.");
            targetTableQuery = build_meta_data_based_target_sql();              //# SQL into _scratch.;
        } //end if
    } catch (FileNotFoundException e) {
        System.out.println("Error 1: FileNotFoundException: "+e
            + "\nThe file: " + inputFileName + " can't be found."
            + "\nIt may be compressed or the case or extension may be different, etc.");
        System.exit(1);
    } catch (IOException e) {
        System.out.println("Error 57: inputIsSourceDbaseSql(): "+e+": "+inputFileName);
        System.exit(57);
    } catch (SQLException e) {
        System.out.println("Error 58: inputIsSourceDbaseSql(): "
            +"SQLException ErrCode: "+e.getErrorCode()+": "+e);
        System.exit(58);
    } catch (Exception e) {
        System.out.println( "Error 59:  input_file_is_sql(): "+e);
        e.printStackTrace();
        System.exit(59);
    } finally { }
    return targetTableQuery;
} //end function { inputIsSourceDbaseSql() }


/**
##########################################################
# Purpose: Set up the meta_data queries.
##########################################################
**/
public static void set_up_meta_data_queries() {
    String metaQuery =null;
    try { //whenever error do_nothing;
        generatedSqlFile.println(  "-- This is the meta data query used to genrate the Target DB SQL." );
        //# Prepare meta-data queries.
        metaQuery =
            " select "+
            " field_name,"+
            " lower(source_field_type),"+
            " lower(source_field_name),"+
            " lower(source_table_name),"+
            " primary_unique_key_ind   "+
            " from meta_dwh_table_field "+
            " where upper(table_name) = '"+targetTable.toUpperCase()+"')"+
            "   and ( upper(source_system_name) = '"+ sourceSystem.toUpperCase()+"'"+
            "      or upper(primary_unique_key_ind) = 'Y' )"+
            "   and design_status != 'Deferred'            "+
            "   and design_status not like '%Remove%'      "+
            "   and design_status not like 'Don%t Use%'    "+
            " order by design_sequence;                    ";
        generatedSqlFile.println(  metaQuery );
        PreparedStatement meta_column_q = metaJdbcConn.prepareStatement(metaQuery);
        metaColCursor = meta_column_q.executeQuery(metaQuery);
        generatedSqlFile.println( "" );
    } catch (SQLException e) {  //whenever error stop
        System.out.println("Warning 130: SQLException ErrCode: "
            +e.getErrorCode()+": "+e+": "+metaQuery);
    } catch (Exception e) {
        System.out.println( "Warning 89: "+e+": "+metaQuery);
        // e.printStackTrace();
    } finally { }
} //end function { set_up_meta_data_queries() }


/**
##########################################################
# Purpose: Build the target targetQuery from meta-data in the database.
##########################################################
 *
 * @return
 */
public static String build_meta_data_based_target_sql() { //function build_meta_data_based_target_sql()
    String targetQuery = null;
    set_up_meta_data_queries();
    try {
        //# Develop the targetQuery for the DWH meta data.
        generatedSqlFile.println( "-- This is the SQL created from the meta data." );
        targetQuery = "select";
        //foreach metaColCursor into {
        while(metaColCursor.next()) {
            targetColumn  = metaColCursor.getString(1);
            sourceColType = metaColCursor.getString(2);
            sourceColName = metaColCursor.getString(3);
            sourceTabName = metaColCursor.getString(4);
            keyIndYN      = metaColCursor.getString(5);
            tgt_col_count++;
            array_boundary( tgt_col_count  );

            //# Add the columns to the select clause.
            targetTableQuery= " "+targetColumn+ ","+c[tgt_col_count]+targetColumn;
            if (keyIndYN.equals("Y")) {
                isKeyColumn[tgt_col_count] = true;
                _key_element = _key_element+1;
                key_pos[_key_element] = tgt_col_count;
            } else {
                isKeyColumn[tgt_col_count] = false;
            } //end if

            //# Record the meta data of each column.
            _srcMetaCol[tgt_col_count] = sourceColName;
            _srcMetaTab[tgt_col_count] = sourceTabName;
            _srcMetaType[tgt_col_count] = sourceColType;
            _comparisonType[tgt_col_count] = data_type( sourceColType        );
        } //end foreach
        if (tgt_col_count == 0) {  sql_error_handle( "_col_count",15); } //end if;
            _key_count = _key_element;
            targetQuery = targetQuery.replaceAll(",$","")+ //# Remove trailing comma.
                " from "+ targetTable+
                " where";
        //# Build the where clause from the key elements.
        for (int i=1; i<=_key_element; i++) {
            targetQuery += " "+ c[key_pos[i]]+ " = ? and";
        } //end for
         targetQuery = targetQuery.replaceAll("and$",";"); //# Remove trailing "and".;
    } catch (SQLException e) {
        System.out.println( "Error 68: SQLException ErrCode: "
            +e.getErrorCode()+": "+e);
        System.exit(68);
    } catch (Exception e) {
        System.out.println( "Error 90: "+e);
        System.exit(90);
    } finally { }
    return targetQuery;
} //end function {  build_meta_data_based_target_sql()  }


/**
##########################################################
# Purpose: Trap array boundary violations.
##########################################################
 *
 * @param n
 */
public static void array_boundary( int n ) { //function array_boundary( n )
    if (n > siz) {
        System.out.println(
            "Error 2: Recompile this program with array size of at least: "+n+
            " to hold all the columns in this table. Change the global siz variable.");
        System.exit(2);
    } //end if
} //end function {  array_boundary( n )  }


/**
##########################################################
# Purpose: Derive one of three basic types for comparison purposes.
##########################################################
 *
 * @param type_
 * @return
 */
public static char data_type( String type_ ) { //function data_type( compType_ )
    if ( type_ == null || type_.equals("") ) { return _character; }
    if ( type_.toUpperCase().matches(".*NUM.*")    ||
         type_.toUpperCase().matches(".*DEC.*")    ||
         type_.toUpperCase().matches(".*INT.*")    ||
         type_.toUpperCase().matches(".*FLOAT.*")  ||
         type_.toUpperCase().matches(".*DOUBLE.*") ||
         type_.toUpperCase().matches(".*LONG.*")   ||
         type_.toUpperCase().matches(".*PIC9.*")   ||
         type_.toUpperCase().matches(".*PIC *9.*")
    ) {
        return _numeric;
    } else if (type_.toUpperCase().matches(".*DATE.*")) {
        return _date;
    } //end if
    return _character;
} //end function { data_type() }



/**
##########################################################
# Purpose: Sets up the target table targetQuery by opening and
#          reading the HEADER in the first line of data
#          from the input flat-file.
##########################################################
 *
 * @return
 */
public static String inputIsLocalFlatFile() { //function inputIsLocalFlatFile()
    try {
        //# Open the input and do the priming read for the HEADER| record.
        flat_input_file = new BufferedReader(new FileReader(inputFileName));
        String flatLine = flat_input_file.readLine();
        c = flatLine.split("[|]",-2);
        src_field_count=c.length;
        //# Get column names from one of: input_file header; or meta-data.
        if (colNamesInHeader) {
            if (flatLine.startsWith("HEADER|")) {
                targetTableQuery = build_header_based_target_sql();
                lastRowWasHeader = true;
            } else {
                System.out.println("Error 6: The input-file must begin with a HEADER| record");
                System.out.println("         if -H (columns-names-in-the-header) parameter is used.");
                System.exit(6); //exit; // program 6
            } //end if
        } else {
            targetTableQuery = build_meta_data_based_target_sql();
            if (s[0].equals("HEADER") ) {
                if (s.length > 1) {
                    compare_header_column_names();
                    check_if_columns_exist();
                } //end if
                lastRowWasHeader = true;
            } //end if
        } //end if
    } catch (FileNotFoundException FNFE) {
        System.out.println("FileNotFoundException: " + FNFE.getMessage());
        System.out.println("Error 7: The file: " + inputFileName + " can't be found.");
        System.out.println("It may be compressed or the case or extension may be different, etc.");
        System.exit(7);
    } catch (IOException e) {
        System.out.println("Error 69: input_file_is_source_data(): "+e);
        System.exit(69);
    } catch (Exception e) {
        System.out.println( "Error 91: input_file_is_source_data(): "+e);
        System.exit(91);
    } finally { }
    return targetTableQuery;
} //end function { inputIsLocalFlatFile() }

/**
##########################################################
# Purpose: Compare the columns specified in a header
#          with columns specified by the meta data.
# Data eg: HEADER|COL_ONE|COL_TWO|COL_THREE|...|COL_N|
##########################################################
 **/
public static void compare_header_column_names() { //function compare_header_column_names()
    int errs = 0;
    int max = (src_field_count > tgt_col_count ? src_field_count : tgt_col_count); //# The greater one of two.
    for (int i=1; i<=max; i++) {
        if (field[i+1].equalsIgnoreCase(c[i])) {
            errs++;
            if (errs == 1) {  //# Do this only on the first one that differs.
                System.out.println(
                    "Warning 339: The -H command line option was not used, "+
                    "but a header was found in the input-file; and "+
                    "the HEADER and META-DATA mismatch, in the following columns: "
                    );
            } //end if;
            System.out.println("    column: "+i+
                " input-file: "+ field[i+1]+
                " meta-data: "+  see_a_null( c[i] ));
        } //end if
    } //end for
    if (errs >= 1) {
        System.out.println("    Continuing, using the meta-data layout.");
    } //end if
} //end function {  compare_header_column_names()  }


/**
##########################################################
# Purpose: Change nulls to something you can see.
##########################################################
 *
 * @param x
 * @return
 */
public static String see_a_null( String x ) { //function see_a_null( x )
    try {
        if ( x == null) {
            return "Null";
        }
        if ( x.equals("")) {
            return "Empty";
        } //end if
        if ( x.matches("[ ][ ]*")) {
            return Integer.toString(x.length())+(x.length()== 1 ? " Space" : " Spaces" );
        } //end if
    } catch  (Exception e) {
        System.out.println("Error 113: "+e+": "+x);
        System.exit(113);
    }
    return x;
} //end function {  see_a_null( x )  }


/**
##########################################################
# Purpose: Check if the HEADER columns exist in the target table
#          database.
# Data eg: HEADER|COL_ONE|COL_TWO|COL_THREE|...|COL_N|
##########################################################
**/
public static void check_if_columns_exist() { //function check_if_columns_exist()
    int i;
    for (i=1; i<=src_field_count; i++) {  //# Use offset avoiding initial HEADER|.
        column_exists( field[i],i );   //# Test if the column from the header is actually in the table.
    } //end for
} //end function {  check_if_columns_exist()  }


/**
##########################################################
# Purpose: Build the main targetQuery from the header in the input-file
#          and the keys from the command line, using the <_scratch>-pad.
##########################################################
 *
 * @return
 */
public static String build_header_based_target_sql() { //function build_header_based_target_sql()
    String sqlFromHeader = null;
    ResultSet whatMeta_c = null;
    int i = 0;
    //# Set up the meta_data lookup for writing to the spreadsheet when variances are detected.
    try { //# Do not crash in the case where the table is not in this database.;
        PreparedStatement whatMeta_p = metaJdbcConn.prepareStatement(
            "select source_field_type,"+
                  " source_field_name,"+
                  " source_table_name"+
            " from meta_dwh_table_field"+
            " where lower(table_name) = lower('"+targetTable+"')"+
             " and lower(field_name) = lower( ? ) " // The lower() function will slow this query down.
            );
        tgt_col_count = src_field_count -1;        //# Remove one for the HEADER| record type.;
        sqlFromHeader= "select" ;
        forCheckLoop : for (i=1; i<=tgt_col_count; i++) {
            array_boundary( i );
            if ( c[i]==null || c[i].equals("") || c[i].equals("''") ) {
                c[i] = "''";                 //# Handle null column names.
            } else {
                column_exists( c[i],i );
            } //end if
            sqlFromHeader +=" "+c[i]+",";    //# Add the columns to the select clause.
            _comparisonType[i] = _character; //# Default to character data type.
            whatMeta_p.setString(1,c[i]);
            try {
                whatMeta_c = whatMeta_p.executeQuery(); // The lower() function will slow this query down.
            } catch (SQLException e) {
                System.out.println("Warning 116: SQLException ErrCode: "
                    +e.getErrorCode()+": "+e);
                continue forCheckLoop;
            }
            try {
                whatMeta_c.next();
            } catch (SQLException e) {
                System.out.println("Warning 117: SQLException ErrCode: "
                    +e.getErrorCode()+": "+e);
                continue forCheckLoop;
            }
            if (whatMeta_c.isFirst()) {
                _srcMetaType[i]=whatMeta_c.getString(1);
                _srcMetaCol[i]=whatMeta_c.getString(2);
                _srcMetaTab[i]=whatMeta_c.getString(3);
            }
        } //end forCheckLoop
    } catch (SQLException e) {
        System.out.println("Warning 46: build_header_based_target_sql(): "
            +"SQLException ErrCode: "+e.getErrorCode()+": "+e);
        //System.exit(46);
    } catch (Exception e) {
        System.out.println( "Warning 47: build_header_based_target_sql(): "+e);
        //System.exit(47);
    } finally { }
    return sqlFromHeader.replaceAll(",$","")+ //# Remove trailing comma.;
            " from "+ targetTable             //# Add the table name.
            +" "+build_the_where_clause();    //# Add the where clause to the query.
} //end function {  build_header_based_target_sql()  }


/**
##########################################################
# Purpose: Test if the column from the header is actually in the
#          table in the target database without actually
#          getting data from the table.
# Data eg: HEADER|COL_ONE|COL_TWO|COL_THREE|...|COL_N|
##########################################################
 *
 * @param col_
 * @param i
 */
public static void column_exists(String col_,int i) { //function column_exists(col_,i)
    String txt = "select "+ col_+" from "+ targetTable;
    try { //whenever error do_nothing;
        metaColExistsPrep = targetJdbcConn.prepareStatement(txt);
    } catch (SQLException e) { //ie not found.
        System.out.println(
            "Warning 339: Column number: "+i+" - "+col_+",\n\t"+
            "does not exist in the physical target table: \""+ targetTable+"\"\n\t"+
            "and will be treated as if a ('') null empty column had been\n\t"+
            "selected to create the input-file: SQLException \n\t"
            +"ErrCode: "+e.getErrorCode()+": "+e);
        c[i] = "''";                     //# Now set the column to empty string.
    } catch (Exception e) {
        System.out.println( "Error 92: column_exists(): "+e);
        System.exit(92);
    } finally { } //whenever error stop
} //end function {  column_exists()  }


/**
* ##########################################################
* # Purpose: Build a where clause for the target table targetQuery.
* #          This can be done from meta-data or from commandline option -isKeyColumn
* ##########################################################
 *
 * @return
 */
public static String build_the_where_clause() { //function build_the_where_clause()
    String scratch_ = "where ",
           scratch2_ = null; //# Generic string working with text.
    try {
        //# Build the where clause from either meta-data or from command line data.
        if (inputKeyCols == null) {
            //# Use the meta-data to work out the primary key.
            scratch2_ =
                " select field_name, "+
                "        lower(source_field_type), "+
                "        lower(source_field_name), "+
                "        lower(source_table_name) "+
                "  from meta_dwh_table_field "+
                "  where lower(table_name) = lower('"+
                              targetTable+ "') "+
                "    and lower(source_system_name) like lower('%"+
                              sourceSystem+ "%') "+
                "    and upper(primary_unique_key_ind) = 'Y' "+
                "    and design_status not like '%Removed%' "+
                "    and design_status != 'Deferred' "+
                "    and design_status not like 'Don%t Use%' "+
                "  order by design_sequence";
            PreparedStatement primary_key_p = metaJdbcConn.prepareStatement(scratch2_);
            ResultSet primary_key_c = primary_key_p.executeQuery("What ever the SQL_ is");

            //# Initialze the the meta-data key indicator to false.
            for (int i=1; i<=tgt_col_count; i++) {
                isKeyColumn[i] = false;
            } //end for

            //# Get the meta-data keys.
            while (primary_key_c.next()) {      //foreach primary_key_c into
                targetColumn  = primary_key_c.getString(1);
                sourceColType = primary_key_c.getString(2);
                sourceColName = primary_key_c.getString(3);
                sourceTabName = primary_key_c.getString(4);

                _key_count++;
                //# Find the matching column and set the key indicator and data type.
                for (int i=1; i<=tgt_col_count; i++) {
                    if (c[i].equals(targetColumn) ) {
                        isKeyColumn[i] = true;
                        _srcMetaType[i] = sourceColType;
                        _srcMetaCol[i] = sourceColName;
                        _srcMetaTab[i] = sourceTabName;
                        scratch_ += make_where_element( i,_key_count );
                        break; // for each
                    } //end if
                } //end for
            } //end foreach
            if (_key_count == 0) {
                System.out.println("Error 14: Key columns missing from table META_DWH_TABLE_FIELD.");
                System.out.println("Make sure the meta data PRIMARY_UNIQUE_KEY_IND is 'Y' for");
                System.out.println("the Pkey, or provide the Pkey column-nos on the command line");
                System.out.println(" See SQL: "+ scratch2_);
                System.exit(14); //exit program 14
            } //end if
        } else {
            //# Use the key elements specified on the command line. #MB#
            if (inputKeyCols.length() == 1) {
                scratch_ +=
                    make_where_element( Integer.parseInt(inputKeyCols),1 );
                _key_count = 1;
            } else {
                split_up_command_line_keys();
                for (int i=1; i<=_key_count; i++) {
                    scratch_ += make_where_element( key_pos[i],i );
                } //end for
            } //end if
        } //end if
        return scratch_.substring(0,(scratch_.length()-3)); //# Remove last "and".
    } catch (SQLException e) {
        System.out.println( "Error 70: build_the_where_clause(): "
            +"SQLException ErrCode: "+e.getErrorCode()+": "+e);
        //e.printStackTrace();
    } finally { }
    return null;
} //end function { build_the_where_clause() }


/**
##########################################################
# Purpose: Split up "-k" command line keys and trap out of range limits.
##########################################################
 * Note: String to integer conversion.
 * http://www.devdaily.com/java/edu/qanda/pjqa00010.shtml
 **/
public static void split_up_command_line_keys() { //function split_up_command_line_keys()
    System.out.println(DateUtils.now("HH:mm:ss")+
        " Getting the key columns from the -k command line option.");
    key_pos_str =inputKeyCols.split(",",-2);
    int i = 0;
    _key_count = key_pos_str.length;
    if (_key_count < 1 || _key_count > maxKeys) {  //Limits trap.
        sql_error_handle( "_key_count",9 );
    } //end if
    try {
        for ( i=0; i<_key_count; i++) {
            if (key_pos_str[i].equals("") || key_pos_str[i] == null) {
                i++;
                System.out.println("Warning 340: commandline option -k \""+inputKeyCols+"\"");
                System.out.println("\t was used, but one element in position "+i);
                System.out.println("\t is null so it was ignored with the rest of the -k arguments beyond that.");
                System.out.println("\t Tip: look for consecutive commas like 1,2,,3 or a trailing comma like \"1,2,3,\" instead of \"1,2,3\".");
                i--;
                _key_count = i;
                return; //exit for loop
            }
            key_pos[(i+1)] = Integer.parseInt(key_pos_str[i].trim()); // The key_pos[ition] index should start at 1 for understandability.
        }
    } catch (NumberFormatException nfe) {
        System.out.println("Error 10: split_up_command_line_keys(): " + nfe.getMessage());
    } catch (Exception e) {
        System.out.println( "Error 94: split_up_command_line_keys(): "+e);
        //e.printStackTrace();
    }
    return;
} //end function { split_up_command_line_keys() }



    /**
    //##########################################################
    //# Purpose: Place the keys into the where clause of the target data targetQuery
    //#          and record them in the global structures.
    //##########################################################
     *
     * @param column_no
     * @param key_element_no
     * @return
     */
public static String make_where_element( int column_no,int key_element_no ) {
    // column_no,              //# The column no. of the key.
    // key_element_no = null;, //# The offset no. of the key element in the key.
    String scratch_ = "";
    if (c[column_no] != null &&  !(c[column_no].equals("''"))) {
        isKeyColumn[column_no] = true;
        key_pos[key_element_no] = column_no;
        scratch_ = scratch_+
            " "+ c[column_no]+ " = ? and";
    } else {
        System.out.println(
            "Warning 347: The index key column name in position: "+
            key_element_no+
            "\n\t of the key for the tagret lookup query,"+
            "\n\t which is input-data column no. "+
            column_no+ ", is blank and has been ignored.  "+
            "\n\t Examine the meta-data keys and the -k command-line options."+
            "\n\t Also, remember to use the 'as column-synonym' syntax in source.SQL files"+
            "\n\t that exactly match the column names in the target table.");
    } //end if
    return scratch_;

} //end function {  make_where_element( i )  }

    /**
     * ###################################################################
     * Purpose: Test if the table from the command-line is actually in the
     *          target database.  Do this by trying to prepare a simple
     *          targetQuery on the table without running it.
     * ###################################################################
    function table_exists( table_name )
     *
     * @param table_name
     * @throws java.sql.SQLException
     */
    public static void table_exists(String table_name) throws SQLException {
        String query = null;
        query = "select * from " + table_name;
        PreparedStatement tab_exists_p = targetJdbcConn.prepareStatement(query);
        if (tab_exists_p != null) {
            tab_exists_p.close(); // free the PreparedStatement.
         } //end if
    } //end function {  table_exists()  }

/**
 * ###################################################################
 *  Purpose: SQL handler for this program.
 * ###################################################################
 *
 * @param txt
 * @param meta_err
 */
public static void sql_error_handle(String txt, int meta_err) { //function sql_error_handle( txt,meta_err )
    switch (meta_err) {
        case (8):
            System.out.println(
                    "Error 8: The target table: '" + targetTable + "\n" +
                    "', does not exist in the database: '" + targetDBase + "\n" +
                    "'.  Examine your meta_qa command line parameters and try 'meta_qa -help'.");
            System.exit(8); //exit program 8
            break;
        case (9):
            System.out.println("Error 9: The min/max number of key columns specified in " +
                    "-k must be between 1 and 20. Ran with: -k " + inputKeyCols);
            System.exit(9); //exit program 9
            break;
        case (15):
            System.out.println("Error 15: Could not find the meta-data with which to build the TARGET query.");
            System.out.println("          for table-name: " + targetTable);
            System.out.println("          and source system-name: " + sourceSystem);
            System.out.println("          Try using the query written to the local .sql file.");
            System.exit(15); //exit program 15
            break;
        case (16):
            System.out.println(
                    "Error 16: The .sql file does not seem to contain the as" +
                    " clause in the select statement.   Add synonym column names" +
                    " that match the target table column names. ");
            System.exit(16); //exit program 16
            break;
        case (17):
        /**
     *        System.out.println("Error 17: Could not find a meta-data ");
     *        System.out.println("primary_unique_key_ind entry for table, field: ",);
     *        table_, ", ", column_
     *        System.out.println("using this targetQuery: ");
     *        System.out.println("    select primary_unique_key_ind, source_field_type");
     *        System.out.println("    from meta_dwh_table_field");
     *        System.out.println("    where upper(table_name) = upper('", table_, "')");
     *        System.out.println("      and upper(field_name) = upper('", column_, "')");
     *        System.out.println("");
     *        System.out.println("To fix this do one of the following:");
     *        System.out.println("   1) Edit your input file: ", inputFileName);
     *        System.out.println("Or 2) Make sure this entry can be found in the meta-data");
     *        System.out.println("      as per the targetQuery above.");
     *        System.exit(17); //exit program 17
     *        break;
         **/
        case (19):
            System.out.println(
                    "Error 19: The .sql file does not seem to contain a 'select' clause.");
            System.exit(19); //exit; // program 19
            break;
        case (20):
            System.out.println(
                    "Error 20: The .sql file does not seem to contain a 'from' clause.");
            System.exit(20); //exit program 20
            break;
        case (35):
            System.out.println(
                    "Error 35: This SQL statement failed with this error: "); //+
            //sql_err()+ //using "<<<<&"+
            //" SQLmsq: "+ sql_err_get( sql_err() ));
            System.exit(35); //exit program 35
            break;
        case (36):
            System.out.println(_scratch);
            System.out.println(
                    "Error 36: This SQL statement failed with this error: " //+
                    //sql_err() using "<<<<&"+
                    // " SQLmsq: ", sql_err_get( sql_err() )
                    );
            System.exit(36); //exit program 36
            break;
        case (3):
            System.out.println("Error 43: The file: " + tempTargetTabSqlFile +
                    " can't be found.");
            System.out.println("It may be compressed or the switch ( or extension may be different) {, etc.");
            System.exit(43); //exit program 37
            break;
        case (38):
            System.out.println(
                    "Error 38: The target table: '" + targetTable +
                    "', already exists in the database: '" + targetDBase +
                    "'.  Check that the temporary table name in " +
                    tempTargetTabSqlFile + " is correct.");
            System.exit(38); //exit program 38
            break;
        case (39):
            System.out.println(txt);
            System.out.println(
                    "Error 39: This SQL statement failed with this error: " +
                    //sql_err() using "<<<<&"
                    " SQLmsq: " //, sql_err_get( sql_err() )
                    );
            System.out.println("Error position in SQL text: " //+ sqlca.sqlerrd[5] using "<<<<<"
                    );
            System.exit(39);
            break;
        case (40):
            System.out.println(txt);
            System.out.println(
                    "Error 40: This SQL statement failed with this error: " //sql_err() using "<<<<&",
                    + " SQLmsq: " //, sql_err_get( sql_err() )
                    );
            System.out.println("Error position in SQL text: "); //+ sqlca.sqlerrd[5] using "<<<<<";
            System.exit(40); //exit program 40
            break;
        case (41):
            System.out.println(txt);
            System.out.println(
                    "Error 41: This SQL statement failed with this error: " +
                    //sql_err() using "<<<<&",
                    " SQLmsq: " //, sql_err_get( sql_err() )
                    );
            System.out.println("Error position in SQL text: " //+ sqlca.sqlerrd[5] using "<<<<<";
                    );
            System.exit(41); //exit; // program 41
            break;
        default:
            System.out.println("Invalid and unknown error code: " + meta_err);
    } //end switch {
} //end function { sql_error_handle( meta_err,txt ) }



/**
 * ###################################################################
 * Purpose: Run the create-temp-target-table SQL script, one statement
 *          at a time.  ie. implements the "-c" commanline option.
 * ###################################################################
// some test code to find out where the current directory of netBeans is.
//PrintWriter pw = new PrintWriter(new FileWriter("meta_test.dat"),true);
//pw.println("some test code to find out where the current directory of netBeans is.");
 *
 * @param tmpTgtTblSqlFileName
 */
public static void runTargetSqlSetupFile(String tmpTgtTblSqlFileName) {
    String query[] = null;
    int i = 0;
    System.out.println(DateUtils.now("HH:mm:ss")+
        " Processing the target SQL file: "+tmpTgtTblSqlFileName);
    try {
        String tempTabSql = "", sqlLine = "";
        BufferedReader tempTargetTableSqlFile =
            new BufferedReader(
            new InputStreamReader(
            new FileInputStream(tmpTgtTblSqlFileName)));            //#open_ascii( sql_file ) returning _end_of_file;        do { //while loop
        do {
            tempTabSql+=" "+sqlLine.replaceFirst("--.*","").trim(); //# Keep everything before a comment and toss the rest.
            sqlLine = tempTargetTableSqlFile.readLine();
        } while (sqlLine != null) ; //end while
        query = tempTabSql.split(";",0);                            //# Separate the queries on semicolon.
        for (i=0;i<query.length;i++) {                              //# Execute all but the last targetQuery.
            processSqlStatement(query[i],targetJdbcConn);           //# NB. no semicolons to follows JDBC statements.
        }
        tempTargetTableSqlFile.close();
    } catch (IOException e) {
        System.out.println("Error 37: run_sql_file(): "
            + tmpTgtTblSqlFileName+": "+e);
        System.out.println(e.getMessage());
        System.exit(37);
    } catch (Exception e) {
        System.out.println( "Error 95: run_sql_file(): "
            + tmpTgtTblSqlFileName+": "+e);
        //e.printStackTrace();
    } finally {  }
} //end function { runTargetSqlSetupFile() }


/**
 * ##########################################################
 * # Purpose: Process sql statements.
 * #          Need special code for "load" statements. This is
 * #          a problem since it is not an ansi standard and thus
 * #          not available on all SQL compliant database engines.
 * # See:     do_load_sql()
 * ##########################################################
 *
 * @param sql_
 * @param conn A connection object pointer is passed in.
 */
public static void processSqlStatement( String sql_, Connection conn ) {
    PreparedStatement tempSqlPreped = null;
    System.out.println(DateUtils.now("HH:mm:ss")+
        " Running SQL statement:"+
        "\n\t "+sql_.trim()+";");
    if (sql_.trim().toLowerCase().startsWith("load")) {
        do_load_sql( sql_, conn );
        return;
    } //end if
    if (sql_.trim().toLowerCase().startsWith("unload")) {
        do_unload_sql( sql_, conn );
        return;
    } //end if
    try {
        tempSqlPreped = conn.prepareStatement(sql_);
        tempSqlPreped.execute();
        tempSqlPreped.close();
    } catch (SQLException e) {
        System.out.println(
            "Warning 324: process_sql_statement(): SQLException Errcode: "
            +e.getErrorCode()+": "+e);
        try {
            tempSqlPreped.close();                           //# Release the targetQuery.
            conn.close();                                    //# close the DB connection.
            System.exit(324);
        } catch (SQLException ee) {
            System.out.println(
                "Error 325: process_sql_statement(): SQLException ErrCode: "
                +ee.getErrorCode()+": "+ee);
            System.exit(325);
        } catch (Exception ee) {
            System.out.println( "Error 96: process_sql_statement(): "+ee);
            //e.printStackTrace();
            System.exit(96);
        }
    } catch (Exception e) {
        System.out.println( "Error 97: process_sql_statement(): "+e);
        //e.printStackTrace();
        System.exit(97);
    } finally { } //end finally

} //end function { processSqlStatement() }


/**
 * #########################################################
 *  Purpose: Emulate the various DB manufacturer't SQL load
 *  statement to load data from a file into a temporary table.
 * @param sql_ the query to be run;
 * @param conn
 */
public static void do_load_sql( String sql_, Connection conn) {
    PreparedStatement insertQueryPreped = null;
    String fName = null,   //# File name.
           tName = null;   //# Table name.
    System.out.println( DateUtils.now("HH:mm:ss")+
        " Parsing embedded (non-SQL) load statement.");

    //# Determine what form of the load statement is this, to get the file name.
    sql_ = sql_.toLowerCase();
    if (sql_.trim().matches("^load[ \t]+from[ \t]+.*$")) {
        System.out.println( DateUtils.now("HH:mm:ss")+
            " Compatible Informix or IBM DB2 syntax found.");
        fName = sql_.trim().replaceAll("^load[ \t]+from[ \t]+","").replaceAll("[ \t]+.*","");
        tName = sql_.replaceAll(".*into[ \t]+","").replaceAll("[ \t]+.*","");
    } else if (sql_.trim().matches("^load[ \t]+data[ \t]+infile[ \t]+.*$")) {
        System.out.println( DateUtils.now("HH:mm:ss")+
            " Compatible Microsoft SQL server syntax found.");
        fName = sql_.trim().replaceAll("^load[ \t]+data[ \t]+infile[ \t]+","").replaceAll("[ \t]+.*","");
        tName = sql_.replaceAll(".*into[ \t]+table[ \t]+","").replaceAll("[ \t]+.*","");
    } else if (sql_.trim().matches("load[ \t]+client[ \t]+from[ \t]+.*$")) {
        System.out.println( DateUtils.now("HH:mm:ss")+
            " Compatible IBM DB2 syntax found.");
        fName = sql_.trim().replaceAll("^load[ \t]+.*from[ \t]+","").replaceAll("[ \t]+.*","");
        tName = sql_.replaceAll(".*into[ \t]+","").replaceAll("[ \t]+.*","");
    } else {
        System.out.println(" Error 134: No compatible load syntax found."+
            "\n\tTry the following example SQL syntax in your SQL batch:"+
            "\n\tload from pipe_delimited_example_file.unl insert into TEMP_TABLE_LOADED;");
        System.exit(134);
    }
    //# Insert records from the file into the table.
    BufferedReader loadFile = null;                             //# The load datafile.
    String loadLine = null,
           loadSql = null,
           loadCols[] = null;
    int nCols = 0;
    try { //##MB##
        loadFile = new BufferedReader(
                    new InputStreamReader(
                    new FileInputStream(fName)));               //#open_ascii( fName ) returning _end_of_file;
        loadLine = loadFile.readLine();                         //# Read a line from the file.
        if (loadLine.startsWith("HEADER|")) {
            loadLine = loadFile.readLine();                     //# Ignore the Header record if there is one.
        }
        if (loadLine != null) {
            loadCols = loadLine.split("[|]");                   //# Split record into array.
            nCols = loadCols.length;
            loadSql = "insert into "+tName+" values ("+         //# Compose SQL.
                replicate("?,",nCols).replaceAll(",$",")");     //# Strip last comma.
            insertQueryPreped =
                conn.prepareStatement(loadSql);                 //# Prepare SQL.
        }
        int i = 0;
        while (loadLine != null) {                              //# Loop thru the load file.
            i++;
            loadCols = loadLine.split("[|]");                   //# Split record into array.
            if (nCols != loadCols.length) {                     //# Trap column variations.
                System.out.println("Error 132: do_load_sql(): "+
                    "The load file '"+fName+
                    "' column counts differ,"+
                    "\n\tat line number: "+i+
                    ".\n\tCheck to see if the user data contains embedded column delimiters.");
                System.exit(132);
            }
            for (int j=0;j<nCols;j++) {                         //# Move array to columns.
                insertQueryPreped.setString((j+1), loadCols[j]);//# insertQueryPreped.setArray(0, loadCols); <-# Does not work.
            }
            insertQueryPreped.executeUpdate();                  //# Insert the row.
            loadLine = loadFile.readLine();                     //# Read another load-record.
        } //while loop
        insertQueryPreped.close();                              //# Clean up.
        System.out.println( DateUtils.now("HH:mm:ss")+
            " Loaded "+i+" rows from file "+fName+" into table "+tName);
    } catch (IOException e) {
        System.out.println( "Warning 133: do_load_sql(): "+e+": "+fName);
        //e.printStackTrace();
    } catch (Exception e) {
        System.out.println( "Warning 98: do_load_sql(): "+
            e+": \n\t"+sql_+": \n\t"+loadLine);
        //e.printStackTrace();
    } catch (Error e) { //if (sql_err())
         sql_error_handle( sql_,39 );
    } finally {
        insertQueryPreped = null;                               //# Realease Prepared SQL.
        loadFile = null;                                        //# Realease load datafile.
    }
} //end function { do_load_sql( sql_ ) }


/**
 * #########################################################
 *  Purpose: Emulate the various DB manufacturer't SQL unload
 *  statement to unload data from a table into a file.
 * See regular Expressions: http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html#sum
 *                          http://www.eisber.net/projects/RegularExpressionsWithJava.html
 * http://www.iiug.org/forums/classics/index.cgi/noframes/read/1693
 * #########################################################
 * unload to delimited_new.unl select * from TEMP_FILE_UNLOAD;        (Informix syntax)
 * load from "delimited_new.unl" insert into TEMP_FILE_LOADED;      (Informix syntax)
 * load from 'delimited_new.unl' insert into TEMP_FILE_LOADED;      (Informix syntax)
 * ######################################################################
 * @param sql_
 * @param conn
 */
public static void do_unload_sql( String sql_, Connection conn) {
    String fName = null,     //# File name.
           tName = null,     //# Table name.
           sqlText = null;   //# Query portion.
    System.out.println( DateUtils.now("HH:mm:ss")+
        " Parsing embedded (non-SQL) unload statement.");

    //# Determine what form of the load statement is this, to get the table and file names.
    sql_ = sql_.toLowerCase();
    if (sql_.trim().matches("^unload[ \t]+to[ \t]+.*$")) {
        System.out.println( DateUtils.now("HH:mm:ss")+
            " Compatible Informix syntax found.");
        //# Strip everything before and after the file-name.
        fName = sql_.trim().replaceAll("^unload[ \t]+to[ \t]+[\"']*","")
                           .replaceAll("[\"']*[ \t]+.*","");
        tName = sql_.replaceAll(".*[ \t]+select[ \t]+.*[ \t]+from[ \t]+","")
                    .replaceAll("[ \t]+.*","");
        sqlText = sql_.replaceAll(".*[ \t]+select[ \t]+","select ");
    } else {
        System.out.println(" Error 135: No compatible unload syntax found."+
            "\n\t Try the following example SQL syntax in your SQL batch:"+
            "\n\t unload to delimited_new.unl select * from UNLOAD_TABLE;");
        System.exit(135);
    }
    try {
        PrintWriter unloadFile =  new PrintWriter(
            new FileWriter( fName,false ),true); // Over write.
        PreparedStatement unloadPrep = conn.prepareStatement(sqlText);
        ResultSet unloadCursor = unloadPrep.executeQuery();
        int cols = unloadCursor.getMetaData().getColumnCount(),
            rows=0;
        String unloadLine = "";
        //# Insert records from the table into the file.
        while (unloadCursor.next()) {
            for (int i=1;i<=cols;i++) {
                unloadLine += unloadCursor.getString(i)+"|";
            }
            unloadFile.println(unloadLine);
            unloadLine="";
            rows++;
        }
        unloadFile.close();
        unloadPrep.close();
        unloadCursor.close();
        System.out.println( DateUtils.now("HH:mm:ss")+
            " Unloaded "+rows+" rows from table "+tName+" into file "+fName);
    } catch (SQLException e) {
        System.out.println(
            "Warning 139: do_unload_sql(): ErrCode"+e.getErrorCode()+": "+e+": "+sql_);
    } catch (IOException e) {
        System.out.println( "Warning 136: do_unload_sql(): "+e+": "+sql_);
    } catch (Exception e) {
        System.out.println( "Warning 137: do_unload_sql(): "+e+": "+sql_);
    } catch (Error e) { //if (sql_err())
        System.out.println( "Error 138: do_unload_sql(): "+e+": ");
        //e.printStackTrace();
        System.exit(138);
    } finally { }
} //end function { do_unload_sql( sql_ ) }


/**
##########################################################
# Purpose: Replicates a string <n> times.
##########################################################
 *
 * @param word
 * @param str
 * @param len
 * @return a word-string embedded in the centre of a string
 *         of replicated charaters.
 */
public static String centreLineOf(String word, String str, int len) {
    return
        replicate(str,((len-word.length())/2))
        +word+
        replicate(str,((len-word.length())/2));
}


/**
##########################################################
# Purpose: Replicates a string <n> times.
##########################################################
 *
 * @param str
 * @param n
 * @return
 */
public static String replicate(String str, int n) {
    String new_str = "";
    if (n<0) n=-n; //abs(n)
    for (int i=1;i<=n;i++) {
        new_str+=str;
    }
    return new_str;
}


/**
##########################################################
# Purpose: Builds a targetQuery from a sourceQuery
#          in the case where the input file is a
#          .sql file, for extracting data from the source of the data-
#          compare. Expect to use the "as" clause in the targetQuery, to
#          build up the select on the target table.  The "as" synonym
#          column-names must match those of the target table.
##########################################################
 *
 * @param sql_
 * @return
 */
public static String build_input_sql_based_target_sql(String sql_) {
    String target_sql = "select",
            compType_ = null,
                 key_ = null,
           sqlToken[] = null;
    int i=0, j=1; // j is the column count starting at 1 for ease of use.
    array_boundary(j);
    sqlToken = sql_.trim().split("[ \t]", -2);
    if (!(sqlToken[0].equalsIgnoreCase("select"))) {
        System.out.println("Error: 77 the input sql does not begin with \"select\"");
        System.exit(77);
    }
    prep_get_key_ind_and_type();
    //# Loop through the columns looking for "as" clauses between the "select" and the "from" for column name synonyms.
    while (!(sqlToken[i].equalsIgnoreCase("from"))) {
        array_boundary(j);
        if (sqlToken[i].equalsIgnoreCase("as")) {
            targetColumn = sqlToken[i+1].replaceAll(",.*", "").replaceAll(" from .* ", "");
            c[j] = targetColumn;  // Stripped gumph after the column synonym name.
            column_exists( c[j],j );
            target_sql = target_sql+" "+ targetColumn+",";
            { //# Get the keys for the target lookup from the meta-data.
                String[] ret = get_key_ind_and_type( targetTable, targetColumn                ); //returning key_, compType_; //http://forum.java.sun.com/thread.jspa?threadID=677098&messageID=3951293
                key_  = ret[0];
                compType_ = ret[1];
            }
            _comparisonType[j] = data_type( compType_  );
            if (inputKeyCols == null) { //# ie. No -isKeyColumn option on the command line.  #MB#
                if (key_.equals( "Y")) {
                    isKeyColumn[j] = true;
                    _key_element++;
                    key_pos[_key_element] = j;  //This indexes which columns are keys.
                } else {
                    isKeyColumn[j] = false;
                } //end if
            } //end if
            j++;        //# Count the columns.
        }
        i++;
    }
    j--;
    src_field_count = j;
    tgt_col_count   = j;
    _key_count   = _key_element;
    target_sql = target_sql.replaceFirst(",$", "")+ // Strip off the last comma.
            " from "+targetTable+" "+build_the_where_clause(); //# Take off the last comma.
    return target_sql;
} //end function { build_input_sql_based_target_sql() }


/**
##########################################################
# Purpose: Supposed to be a fast and efficient way to get the
#          indicators for key columns and the data types of each column.
##########################################################
 * JDBC Example code:
 * http://www.jdbc-tutorial.com/jdbc-prepared-statements.htm
**/
public static void prep_get_key_ind_and_type() { //function prep_get_key_ind_and_type()
    String sql_ = null;
    try { //whenever error continue
        sql_ =
            "select upper(primary_unique_key_ind),    "+
            "       upper(source_field_type)          "+
            " from meta_dwh_table_field               "+
            " where table_name = ? and field_name = ? ";
        metaKeyTypePrep = metaJdbcConn.prepareStatement(sql_);
    } catch (SQLException e) { //whenever error stop
        System.out.println("Error 33: "+". SQLException ErrCode: "
            +e.getErrorCode()+": "+e+". Could not prepare this query: "+sql_);
        System.exit(33); //exit program 33
    } catch (Exception e) {
        System.out.println( "Error 99: prep_get_key_ind_and_type(): "+sql_+": "+e);
        e.printStackTrace();
        System.exit(99); //exit program 33
    } finally { }
} //end function { prep_get_key_ind_and_type() }


/**
##########################################################
# Purpose: Lookup the key inicator from the meta-data.
# Refer:   http://forum.java.sun.com/thread.jspa?threadID=677098&messageID=3951293
#          http://java.sun.com/docs/books/tutorial/jdbc/basics/prepared.html
##########################################################
 *
 * @param table_
 * @param column_
 * @return
 */
public static String[] get_key_ind_and_type( String table_, String column_ ) { //function get_key_ind_and_type( table_, column_ )
    String[] retVals = new String[2];
    ResultSet metaKeyAndTypeCursor = null;          //# Cursor for meta data key and type.
    try {
        metaKeyTypePrep.setString(1,table_);	    //# NB. index starts with 1
        metaKeyTypePrep.setString(2,column_);
        metaKeyAndTypeCursor = metaKeyTypePrep.executeQuery();
        if (metaKeyAndTypeCursor.next()) {
            //retVals[0]= key_ind_c.getString("primary_unique_key_ind");
            retVals[0]= metaKeyAndTypeCursor.getString(1);
            retVals[1]= metaKeyAndTypeCursor.getString("source_field_type".toUpperCase());
        } else {
            retVals[0]= null;
            retVals[1]= null;
        }
        return retVals;
    } catch (SQLException e) {
        System.out.println("Error 34: SQLException: SQLException ErrCode: "
            +e.getErrorCode()+": "+e +", on table: "
            +table_+", and column: "+column_);
        System.exit(34); //exit program 34
    } catch (Exception e) {
        System.out.println( "Error 100: get_key_ind_and_type(): "+e
            +" on table: "+table_+" and column"+column_);
        //e.printStackTrace();
    } finally {
        try {
            metaKeyAndTypeCursor.close();
        } catch (SQLException e) {
            System.out.println(
                "Error 42: get_key_ind_and_type(): SQLException ErrCode: "
                +e.getErrorCode()+": "+e +" on table: "+table_
                +" and column"+column_);
            System.exit(42); //exit program 34
        } catch (Exception e) {
            System.out.println( "Error 108: get_key_ind_and_type(): "+e
                +" on table: "+table_+" and column"+column_);
            //e.printStackTrace();
        }
    }
    return retVals;

    //# If not found, it"t not an error, "cos we can pick it up later with -isKeyColumn ?

} //end function { get_key_ind_and_type( table_, column_ ) }


    /**
##########################################################
# Purpose: Just for testing date functions downloaded from the net.
##########################################################
**/
    public static void Date_test_function() {
    /**
     *     System.out.println(DateUtils.now("yyyy-MM-dd HH:mm:ss"));
     *     System.out.println(DateUtils.now("HH:mm:ss"));
     *     System.out.println(DateUtils.now("yyyyMMdd"));
     *     System.out.println(DateUtils.now("dd.MM.yy"));
     *     System.out.println(DateUtils.now("MM/dd/yy"));
     *     System.out.println(DateUtils.now("yyyy.MM.dd G 'at' hh:mm:ss z"));
     *     System.out.println(DateUtils.now("EEE, MMM d, ''yy"));
     *     System.out.println(DateUtils.now("h:mm a"));
     *     System.out.println(DateUtils.now("H:mm:ss:SSS"));
     *     System.out.println(DateUtils.now("K:mm a,z"));
     *     System.out.println(DateUtils.now("yyyy.MMMMM.dd GGG hh:mm aaa"));
     **/
        System.out.println("Dummy Statement for compiler/editor function grouping.");
    }

} //end of Class Metaqa


//=============================== CLASS Boundary ===============================
/**
 * This code was downloaded from this site:
 * http://www.rgagnon.com/javadetails/java-0106.html
 **/
class DateUtils {

    public static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());

    }

    public static String day(Date inDate, String dateFormatPattern) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatPattern);
        cal.setTime(inDate);
        return sdf.format(cal.getTime());

    }
} //end of Class DateUtils
// End of file.