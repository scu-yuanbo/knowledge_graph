# Data Design

## 数据库设计

### 实体
1. author - 作者
2. paper - 论文
3. organization - 单位
4. year - 年份
5. journal - 期刊
6. keyword - 关键字
7. imburse - 项目基金

1. author - 作者
- author.csv

  | author_id    | author_name | 
  | ------ | -------- | 
  | int,11 | longtext | 

2. paper - 论文
- paper.csv

  | paper_id | paper_name | paper_class | 
  | ----------- | -------- | -------- |
  | varchar,64 | longtext | longtext |

3. organization - 单位
- organization.csv

  | org_id    | org_name  |
  | ------ | ------------ |
  | int,11 | longtext |

4. year-年份
  | year_id | year |
  | ------ | ------------ |
  | int,11 | longtext |

5. journal-期刊
- journal.csv

  | journal_id  | journal_name  |
  | ------ | ------- |
  | int,11 | varchar |

6. keyword - 关键字
- keyword.csv

  | keyword_id  | keyword_name  |
  | ------ | -------- |
  | int,11 | longtext |

7. imburse - 项目基金

- imburse.csv

  | imburse_id    | imburse_name   |
  | ------ | -------- |
  | int,11 | longtext |

### 关系
1. author (write)-> paper
2. author (belong-to)-> organization  
3. paper (belong-to)-> journal  
4. paper (time)-> year
5. paper (belong-to)-> imburse 
6. paper (about)-> keyword

### TODO
一些实体之间具有层次与关联关系，例如：  
组织：「北京大学医学院」和「北京大学计算机学院」属于「北京大学」  
关键字：「卷积神经网络」和「支持向量机」属于「机器学习」和「人工智能」相关