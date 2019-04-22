
public class Mysql_to_neo4j {

    private boolean isSucess = false;

    public Boolean index() throws ClassNotFoundException, SQLException {
        String[] authorArray;
        String[] keywordArray;
        String[] orgArray;
        String[] imburseArray;
        String[] aoArray;
        long author_id = 0;
        long paper_id = 0;
        long org_id = 0;
        long journal_id = 0;
        long keyword_id = 0;
        long imburse_id = 0;
        //mysql数据库连接
        Class.forName(Constant.driver);
        Connection con = DriverManager.getConnection(Constant.databaseUrl, Constant.username, Constant.password);
        if (!con.isClosed()) {
            System.out.println("mysql数据库连接成功！");
        }
        PreparedStatement mysql_ps = con.prepareStatement("SELECT * FROM ArticleInfo_2010 limit 15");
        ResultSet m_result = mysql_ps.executeQuery();

        //neo4j数据库连接
        Driver driver = GraphDatabase.driver("bolt://192.168.229.204:7687",
                AuthTokens.basic("neo4j", "yb1135"));
        System.out.println("neo4j数据库连接成功！");
        Session session = driver.session();

        //初始化id
        author_id = session.run("MATCH (o:czc_Person) RETURN COUNT(o) AS count").next().get("count").asInt();
        paper_id = session.run("MATCH (o:czc_Paper) RETURN COUNT(o) AS count").next().get("count").asInt();
        org_id = session.run("MATCH (o:czc_Organ) RETURN COUNT(o) AS count").next().get("count").asInt();
        journal_id = session.run("MATCH (o:czc_Journal) RETURN COUNT(o) AS count").next().get("count").asInt();
        keyword_id = session.run("MATCH (o:czc_Keyword) RETURN COUNT(o) AS count").next().get("count").asInt();
        imburse_id = session.run("MATCH (o:czc_Imburse) RETURN COUNT(o) AS count").next().get("count").asInt();

        while (m_result.next()) {
            //提取实体
            String author = m_result.getString("Author");
            String paper = m_result.getString("Title");
            String org = m_result.getString("Organ");
            String year = m_result.getString("Year");
            String journal = m_result.getString("JournalName");
            String keyword = m_result.getString("Keyword");
            String imburse = m_result.getString("Imburse");
            String a_o = m_result.getString("AuthorOrgan");
            StatementResult paper_result;
            StatementResult org_result;
            StatementResult journal_result;
            StatementResult imburse_result;

            //添加paper实体
            paper_result = session.run("MATCH (p:czc_Paper) WHERE p.paper_name =\"" + paper.trim() + "\" RETURN p");
            if (!paper.isEmpty() && (!paper_result.hasNext()) && (!author.isEmpty())) {
                paper_id++;
                session.run("CREATE (p:czc_Paper {paper_id: " + paper_id + ", paper_name: \"" + paper.trim()
                        + "\", paper_class: \"" + m_result.getString("Class") + "\"})");

                if (!year.isEmpty()) {
                    //添加paper-year关系
                    session.run("MATCH (p:czc_Paper),(y:czc_Year) WHERE p.paper_id =" + paper_id
                            + " AND y.year =\"" + year.trim() + "\" CREATE (p)-[r:CONNECT]->(y) RETURN r");
                }
                //添加author实体
                authorArray = author.split(";");
                for (int i = 0; i < authorArray.length; i++) {
                    if (!authorArray[i].trim().isEmpty()) {  //判空（必要）
                        author_id++;
                        session.run("CREATE (p:czc_Author {author_id: " + author_id
                                + ", author_name: \"" + authorArray[i].trim() + "\"})");
                        //添加author-paper关系
                        session.run("MATCH (a:czc_Author),(p:czc_Paper) WHERE a.author_id =" + author_id + " AND p.paper_id =" + paper_id
                                + " CREATE (a)-[r:CONNECT]->(p) RETURN r");
                    }
                }
            } else continue;
            //添加organ实体
            if (!org.isEmpty()) {  //判空
                orgArray = org.split(";");
                for (int i = 0; i < orgArray.length; i++) {
                    org_result = session.run("MATCH (o:czc_Organ) WHERE o.org_name = \""
                            + orgArray[i].trim() + "\" RETURN o");
                    if (!org_result.hasNext()) {  //去重
                        org_id++;
                        session.run("CREATE (o:czc_Organ {org_id: " + org_id
                                + ", org_name: \"" + orgArray[i].trim() + "\"})");
                    }
                }
            }
            //添加journal实体
            if (!journal.isEmpty()) {  //判空
                journal_result = session.run("MATCH (j:czc_Journal) WHERE j.journal_name = \""
                        + journal.trim() + "\" RETURN j");
                if (!journal_result.hasNext()) {  //去重
                    journal_id++;
                    session.run("CREATE (j:czc_Journal {journal_id: " + journal_id
                            + ", jouranl_name: \"" + journal.trim() + "\"})");
                }
                //添加paper-journal关系
                session.run("MATCH (p:czc_Paper),(j:czc_Journal) WHERE p.paper_id =" + paper_id + " AND j.journal_name =\"" + journal.trim()
                        + "\" CREATE (p)-[r:CONNECT]->(j) RETURN r");
            }
            //添加imburse实体
            if (!imburse.isEmpty()) {  //判空
                imburseArray = imburse.split("；");
                for (int i = 0; i < imburseArray.length; i++) {
                    if (!imburseArray[i].trim().isEmpty()) {
                        imburse_result = session.run("MATCH (i:czc_Imburse) WHERE i.imburse_name = \"" + imburseArray[i].trim() + "\" RETURN i");
                        if (!imburse_result.hasNext()) {  //去重
                            imburse_id++;
                            session.run("CREATE (i:czc_Imburse {imburse_id: " + imburse_id
                                    + ", imburse_name: \"" + imburseArray[i].trim() + "\"})");
                        }
                        //添加paper-imburse关系
                        session.run("MATCH (p:czc_Paper),(i:czc_Imburse) WHERE p.paper_id =" + paper_id + " AND i.imburse_name =\"" + imburseArray[i].trim()
                                + "\" CREATE (p)-[r:CONNECT]->(i) RETURN r");
                    }
                }
            }
            //添加keyword实体
            if (!keyword.isEmpty()) {  //判空
                keywordArray = keyword.split(";");
                for (int i = 0; i < keywordArray.length; i++) {
                    if (!keywordArray[i].trim().isEmpty()) {  //判空
                        StatementResult keyword_result = session.run("MATCH (k:czc_Keyword) WHERE k.keyword_name = \"" + keywordArray[i].trim() + "\" RETURN k");
                        if (!keyword_result.hasNext()) {  //去重
                            keyword_id++;
                            session.run("CREATE (k:czc_Keyword {keyword_id: " + keyword_id
                                    + ", keyword_name: \"" + keywordArray[i].trim() + "\"})");
                        }
                        //添加paper-keyword关系
                        session.run("MATCH (p:czc_Paper),(k:czc_Keyword) WHERE p.paper_id =" + paper_id + " AND k.keyword_name =\"" + keywordArray[i].trim()
                                + "\" CREATE (p)-[r:CONNECT]->(k) RETURN r");
                    }
                }
            }

            //添加author-organ关系
            if (!a_o.isEmpty()) {
                String a = null;
                String o = null;
                aoArray = a_o.split(";");
                for (int i = 0; i < aoArray.length; i++) {
                    a = aoArray[i].split("\\[")[0].trim();
                    o = aoArray[i].split("\\[")[1].trim();
                    o = o.substring(0, o.length() - 1);
                    session.run("MATCH (a:czc_Author),(o:czc_Organ) WHERE a.author_name = \"" + a + "\" AND o.org_name = \"" + o
                            + "\" CREATE (a)-[r:CONNECT]->(o) RETURN r");
                }
            }
        }
        session.close();
        driver.close();

        return isSucess;
    }
}
