package smarticulous;

import smarticulous.db.Exercise;
import smarticulous.db.Submission;
import smarticulous.db.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Smarticulous class, implementing a grading system.
 */
public class Smarticulous {

    /**
     * The connection to the underlying DB.
     * <p>
     * null if the db has not yet been opened.
     */
    Connection db;

    /**
     * Open the {@link Smarticulous} SQLite database.
     * <p>
     * This should open the database, creating a new one if necessary, and set the {@link #db} field
     * to the new connection.
     * <p>
     * The open method should make sure the database contains the following tables, creating them if necessary:
     *
     * <table>
     *   <caption><em>Table name: <strong>User</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>UserId</td><td>Integer (Primary Key)</td></tr>
     *   <tr><td>Username</td><td>Text</td></tr>
     *   <tr><td>Firstname</td><td>Text</td></tr>
     *   <tr><td>Lastname</td><td>Text</td></tr>
     *   <tr><td>Password</td><td>Text</td></tr>
     * </table>
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>Exercise</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>ExerciseId</td><td>Integer (Primary Key)</td></tr>
     *   <tr><td>Name</td><td>Text</td></tr>
     *   <tr><td>DueDate</td><td>Integer</td></tr>
     * </table>
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>Question</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>ExerciseId</td><td>Integer</td></tr>
     *   <tr><td>QuestionId</td><td>Integer</td></tr>
     *   <tr><td>Name</td><td>Text</td></tr>
     *   <tr><td>Desc</td><td>Text</td></tr>
     *   <tr><td>Points</td><td>Integer</td></tr>
     * </table>
     * In this table the combination of ExerciseId and QuestionId together comprise the primary key.
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>Submission</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>SubmissionId</td><td>Integer (Primary Key)</td></tr>
     *   <tr><td>UserId</td><td>Integer</td></tr>
     *   <tr><td>ExerciseId</td><td>Integer</td></tr>
     *   <tr><td>SubmissionTime</td><td>Integer</td></tr>
     * </table>
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>QuestionGrade</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>SubmissionId</td><td>Integer</td></tr>
     *   <tr><td>QuestionId</td><td>Integer</td></tr>
     *   <tr><td>Grade</td><td>Real</td></tr>
     * </table>
     * In this table the combination of SubmissionId and QuestionId together comprise the primary key.
     *
     * @param dburl The JDBC url of the database to open (will be of the form "jdbc:sqlite:...")
     * @return the new connection
     * @throws SQLException
     */
    public Connection openDB(String dburl) throws SQLException {
        //create a connection to the database using the url
        db = DriverManager.getConnection(dburl);
        try (Statement st = db.createStatement()){
            //create user table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS User " + 
                "(UserId INTEGER PRIMARY KEY, Username TEXT UNIQUE, Firstname TEXT,Lastname TEXT, Password TEXT);");
            //create exercise table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS Exercise (ExerciseId INTEGER PRIMARY KEY, Name TEXT, DueDate INTEGER);");
            //create question table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS Question " + 
                "(ExerciseId INTEGER, QuestionId INTEGER, Name TEXT, Desc TEXT, Points INTEGER, PRIMARY KEY (ExerciseId, QuestionId));");
            //create submission table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS Submission " + 
                "(SubmissionId INTEGER PRIMARY KEY, UserId INTEGER, ExerciseId INTEGER, SubmissionTime INTEGER);");
            //create question grade table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS QuestionGrade " + 
                "(SubmissionId INTEGER, QuestionId INTEGER, Grade REAL, PRIMARY KEY (SubmissionId, QuestionId));");
        } 
        return db;
    }


    /**
     * Close the DB if it is open.
     *
     * @throws SQLException
     */
    public void closeDB() throws SQLException {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    // =========== User Management =============

    /**
     * Add a user to the database / modify an existing user.
     * <p>
     * Add the user to the database if they don't exist. If a user with user.username does exist,
     * update their password and firstname/lastname in the database.
     *
     * @param user
     * @param password
     * @return the userid.
     * @throws SQLException
     */
    public int addOrUpdateUser(User user, String password) throws SQLException {
        //this will be returned
        int userId = 0;
        //a query to check if the user exists in the table 
        String existQuery = "SELECT UserId FROM User WHERE Username = '" + user.username + "'";
        String insertString = "INSERT INTO User (Username, Firstname, Lastname, Password) Values (?, ?, ?, ?)";
        String updateString = "UPDATE User SET Firstname = ?, Lastname = ?, Password = ? WHERE Username = '" + user.username + "'";
        try(Statement st = db.createStatement()){
            //checks if user exists in the table
            ResultSet result = st.executeQuery(existQuery);
            if(result.next()){
                //user exists - get his user id
                userId = result.getInt(1);
                try(PreparedStatement updateUser = db.prepareStatement(updateString)){
                    //updates
                    updateUser.setString(1, user.firstname);
                    updateUser.setString(2, user.lastname);
                    updateUser.setString(3, password);
                    //excutes the statement
                    updateUser.executeUpdate();
                }

            } else {
                //user doesnt exist in the table
                try(PreparedStatement insertUser = db.prepareStatement(insertString)){
                    //inserts user
                    insertUser.setString(1, user.username);
                    insertUser.setString(2, user.firstname);
                    insertUser.setString(3, user.lastname);
                    insertUser.setString(4, password);
                    //excutes the statement
                    insertUser.executeUpdate();
                    //gets the user id of the new user
                    ResultSet generatedUser = insertUser.getGeneratedKeys();
                    userId = generatedUser.getInt(1); 
                }
            }         
        }
        return userId;
    }


    /**
     * Verify a user's login credentials.
     *
     * @param username
     * @param password
     * @return true if the user exists in the database and the password matches; false otherwise.
     * @throws SQLException
     * <p>
     * Note: this is totally insecure. For real-life password checking, it's important to store only
     * a password hash
     * @see <a href="https://crackstation.net/hashing-security.htm">How to Hash Passwords Properly</a>
     */
    public boolean verifyLogin(String username, String password) throws SQLException {
        //query to get the user from the table 
        String existQuery = "SELECT Username,Password FROM User WHERE Username = ?";
        try (PreparedStatement existUser = db.prepareStatement(existQuery)){
            //checks if the user is in the table (username is unique)
            existUser.setString(1, username);
            ResultSet result = existUser.executeQuery();
            if(result.next()){
                //user exists - now check the password
                String takenPassword = result.getString(2);
                if(takenPassword.equals(password)){
                    //password matches too
                    return true;
                }
            }
        }
        //not a match
        return false; 
          
    }

    // =========== Exercise Management =============

    /**
     * Add an exercise to the database.
     *
     * @param exercise
     * @return the new exercise id, or -1 if an exercise with this id already existed in the database.
     * @throws SQLException
     */
    public int addExercise(Exercise exercise) throws SQLException {
        //this variable will be returned
        int exerciseIdToReturn = -1;
        String insertString = "INSERT INTO Exercise (ExerciseId, Name, DueDate) Values (?, ?, ?)";
        try (PreparedStatement addExerciPreparedStatement = db.prepareStatement(insertString)){
            //checks if the exercise is in the table
            addExerciPreparedStatement.setInt(1, exercise.id);
            addExerciPreparedStatement.setString(2, exercise.name);
            addExerciPreparedStatement.setInt(3,((int)exercise.dueDate.getTime()));
            //excutes the statement
            addExerciPreparedStatement.executeUpdate();
            //gets the new exercise id 
            ResultSet generatedExercise = addExerciPreparedStatement.getGeneratedKeys();
            //this is a new exercise. insert the questions to questions table.
            if(generatedExercise.next()){
                exerciseIdToReturn = generatedExercise.getInt(1);
                String insertQuestion = "INSERT INTO Question (ExerciseId, Name, Desc, Points) VALUES (?, ?, ?, ?)";
                try (PreparedStatement addQuestionPreparedStatement = db.prepareStatement(insertQuestion)){
                    //iterates questions in the new exercise. adds them.
                    for (Exercise.Question question : exercise.questions){
                        addQuestionPreparedStatement.setInt(1, exerciseIdToReturn);
                        addQuestionPreparedStatement.setString(2,question.name);
                        addQuestionPreparedStatement.setString(3,(question.desc));
                        addQuestionPreparedStatement.setInt(4, question.points);
                        //excutes the statement
                        addQuestionPreparedStatement.executeUpdate();
                    }
                }
            
            }
        }
        return exerciseIdToReturn;

    }


    /**
     * Return a list of all the exercises in the database.
     * <p>
     * The list should be sorted by exercise id.
     *
     * @return list of all exercises.
     * @throws SQLException
     */
    public List<Exercise> loadExercises() throws SQLException {
        //the list to return
        List<Exercise> exercisesList = new ArrayList<>();

        String selectQuery = "SELECT * FROM Exercise ORDER BY ExerciseId";
        try (PreparedStatement preparedStatementExercises = db.prepareStatement(selectQuery)){
            //get and ordered table of the exercises
            ResultSet result1 = preparedStatementExercises.executeQuery();
            while(result1.next()){
                //build an exercise from the data 
                int ExerciseId = result1.getInt(1);
                String Name = result1.getString(2);
                int DueDate = result1.getInt(3);
                
                long DuedateLong = (long) DueDate;
                Date dueDate = new Date(DuedateLong);
                Exercise exercise = new Exercise(ExerciseId, Name, dueDate);
                //getting the question of this exercise
                String questionQuery = "SELECT * FROM Question WHERE ExerciseId = ?";
                try(PreparedStatement preparedStatementQuestion = db.prepareStatement(questionQuery)){
                    preparedStatementQuestion.setInt(1, ExerciseId );
                    ResultSet result2 = preparedStatementQuestion.executeQuery();

                    while(result2.next()){
                        //get question data and add the question to the exercise's question list
                        String questionName = result2.getString(3);
                        String Desc = result2.getString(4);
                        int Points = result2.getInt(5);

                        exercise.addQuestion(questionName, Desc, Points);
                    } 



                }
            //add exercise to the list
            exercisesList.add(exercise);

            }

        }
        return exercisesList;
    }

    // ========== Submission Storage ===============

    /**
     * Store a submission in the database.
     * The id field of the submission will be ignored if it is -1.
     * <p>
     * Return -1 if the corresponding user doesn't exist in the database.
     *
     * @param submission
     * @return the submission id.
     * @throws SQLException
     */

    public int storeSubmission(Submission submission) throws SQLException {
        //check if the user exists in the data base
        if(getUserId(submission.user) == -1){
            //the user doesnt exist
            return -1;
        }
        //insert the subnission to the table
        String insertSubmissioString = "INSERT INTO Submission (SubmissionId, UserId, ExerciseId, SubmissionTime) VALUES (?, ?, ?, ?)";
        try(PreparedStatement submissioPreparedStatement = db.prepareStatement(insertSubmissioString)){
            if (submission.id != -1) {
                submissioPreparedStatement.setInt(1, submission.id);           
            }
            submissioPreparedStatement.setInt(2, getUserId(submission.user));           
            submissioPreparedStatement.setInt(3, submission.exercise.id);           
            submissioPreparedStatement.setLong(4, ((long)submission.submissionTime.getTime()));
            submissioPreparedStatement.executeUpdate();

            //get submission id
            if(submission.id == -1){
                ResultSet generatedSubmission = submissioPreparedStatement.getGeneratedKeys();
                if(generatedSubmission.next()){
                    submission.id = generatedSubmission.getInt(1); 
                }
            }
            
            //gets question id and caluclates grade for all the questions of the exercise submitted.
            //inserts them to QuestionGrade table.
            String selectQuestion = "SELECT QuestionId, Points FROM Question WHERE ExerciseId = ?";
            try (PreparedStatement questionPreparedStatement = db.prepareStatement(selectQuestion)){
                questionPreparedStatement.setInt(1, submission.exercise.id);
                ResultSet result = questionPreparedStatement.executeQuery();
                //iterates the reuslts
                int counter = 0;
                while(result.next()){
                    int questionid = result.getInt(1);
                    float grade = submission.questionGrades[counter] / result.getInt(2);
                    counter++;

                    //inserts the question grade and question id for each question in the exercise.
                    String questionGradeString = "INSERT INTO QuestionGrade (SubmissionId, QuestionId, Grade) VALUES (?, ?, ?)";
                    try(PreparedStatement questionGradePreparedStatement = db.prepareStatement(questionGradeString)){
                        questionGradePreparedStatement.setInt(1, submission.id);           
                        questionGradePreparedStatement.setInt(2, questionid);
                        questionGradePreparedStatement.setFloat(3, grade);
                        questionGradePreparedStatement.executeUpdate();
                        
                    }
       
                }
            }       
        }

        return submission.id;
    }


    // ============= Submission Query ===============

    //private method to get the user id from a user object
    //returns the UserId if the user exists, otherwise returnes -1
    private int getUserId(User user) throws SQLException{
        String userString = "SELECT UserId FROM User WHERE Username = ?";
        try(PreparedStatement userPreparedStatement = db.prepareStatement(userString)){
            userPreparedStatement.setString(1, user.username);
            ResultSet result = userPreparedStatement.executeQuery();
            if(result.next()){
                return result.getInt(1);
            }
        }
        return -1;      
    }


    /**
     * Return a prepared SQL statement that, when executed, will
     * return one row for every question of the latest submission for the given exercise by the given user.
     * <p>
     * The rows should be sorted by QuestionId, and each row should contain:
     * - A column named "SubmissionId" with the submission id.
     * - A column named "QuestionId" with the question id,
     * - A column named "Grade" with the grade for that question.
     * - A column named "SubmissionTime" with the time of submission.
     * <p>
     * Parameter 1 of the prepared statement will be set to the User's username, Parameter 2 to the Exercise Id, and
     * Parameter 3 to the number of questions in the given exercise.
     * <p>
     * This will be used by {@link #getLastSubmission(User, Exercise)}
     *
     * @return
     */
    PreparedStatement getLastSubmissionGradesStatement() throws SQLException {
        //selects the correct columns
        String selectPart = "SELECT Submission.SubmissionId, QuestionId, Grade, SubmissionTime ";
        //inner joins the tables
        String fromPart = "FROM Submission INNER JOIN QuestionGrade ON Submission.SubmissionId = QuestionGrade.SubmissionId ";
        //smaller queries to build the where part
        String innerSelectWhere = "(SELECT UserId FROM User WHERE Username = ?)";
        //where part of the query
        String wherePart = "WHERE UserId = " + innerSelectWhere + " AND ExerciseId = ? ";
        //order by part
        String orderByPart = "ORDER BY SubmissionTime DESC, QuestionId ";
        //limit
        String limitPart = "LIMIT ?";
        
        //join the parts to get complete query
        String completeQuery = selectPart + fromPart + wherePart + orderByPart + limitPart;

        PreparedStatement queryPreparedStatement = db.prepareStatement(completeQuery);
        return queryPreparedStatement;
    }

    /**
     * Return a prepared SQL statement that, when executed, will
     * return one row for every question of the <i>best</i> submission for the given exercise by the given user.
     * The best submission is the one whose point total is maximal.
     * <p>
     * The rows should be sorted by QuestionId, and each row should contain:
     * - A column named "SubmissionId" with the submission id.
     * - A column named "QuestionId" with the question id,
     * - A column named "Grade" with the grade for that question.
     * - A column named "SubmissionTime" with the time of submission.
     * <p>
     * Parameter 1 of the prepared statement will be set to the User's username, Parameter 2 to the Exercise Id, and
     * Parameter 3 to the number of questions in the given exercise.
     * <p>
     * This will be used by {@link #getBestSubmission(User, Exercise)}
     *
     */
    PreparedStatement getBestSubmissionGradesStatement() throws SQLException {
        // TODO: Implement
        return null;
    }

    /**
     * Return a submission for the given exercise by the given user that satisfies
     * some condition (as defined by an SQL prepared statement).
     * <p>
     * The prepared statement should accept the user name as parameter 1, the exercise id as parameter 2 and a limit on the
     * number of rows returned as parameter 3, and return a row for each question corresponding to the submission, sorted by questionId.
     * <p>
     * Return null if the user has not submitted the exercise (or is not in the database).
     *
     * @param user
     * @param exercise
     * @param stmt
     * @return
     * @throws SQLException
     */
    Submission getSubmission(User user, Exercise exercise, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, user.username);
        stmt.setInt(2, exercise.id);
        stmt.setInt(3, exercise.questions.size());

        ResultSet res = stmt.executeQuery();

        boolean hasNext = res.next();
        if (!hasNext)
            return null;

        int sid = res.getInt("SubmissionId");
        Date submissionTime = new Date(res.getLong("SubmissionTime"));

        float[] grades = new float[exercise.questions.size()];

        for (int i = 0; hasNext; ++i, hasNext = res.next()) {
            grades[i] = res.getFloat("Grade");
        }

        return new Submission(sid, user, exercise, submissionTime, (float[]) grades);
    }

    /**
     * Return the latest submission for the given exercise by the given user.
     * <p>
     * Return null if the user has not submitted the exercise (or is not in the database).
     *
     * @param user
     * @param exercise
     * @return
     * @throws SQLException
     */
    public Submission getLastSubmission(User user, Exercise exercise) throws SQLException {
        return getSubmission(user, exercise, getLastSubmissionGradesStatement());
    }


    /**
     * Return the submission with the highest total grade
     *
     * @param user the user for which we retrieve the best submission
     * @param exercise the exercise for which we retrieve the best submission
     * @return
     * @throws SQLException
     */
    public Submission getBestSubmission(User user, Exercise exercise) throws SQLException {
        return getSubmission(user, exercise, getBestSubmissionGradesStatement());
    }
}
