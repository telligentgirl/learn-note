# 接口参数核对（依据《学习笔记接口文档-V1.0》）

> 最新产品调整：笔记采用纯文字形式，新增、编辑不再要求 coverImg。下表保留原接口文档的契约供对照；当前实现中 coverImg 为可选。新建时后端将缺省封面存为空字符串以兼容旧表 NOT NULL，编辑时省略则保留历史封面。页面不再输入或展示封面。

所有受保护接口使用 Authorization 原始 Token，不加 Bearer。响应先检查 HTTP 状态，再检查 code=0，业务数据在 data。

| 接口 | 参数位置 | 必须参数 | 可选参数 |
|---|---|---|---|
| POST /user/register | URL 编码表单 | username、password（5～16 非空白字符） | 无 |
| POST /user/login | URL 编码表单 | username、password | 无 |
| GET /user/userInfo | 无 | 无 | 无 |
| PUT /user/update | JSON | id、nickname、email | username |
| PATCH /user/updateAvatar | query | avatarUrl | 无 |
| PATCH /user/updatePwd | JSON | old_pwd、new_pwd、re_pwd | 无 |
| POST /category | JSON | categoryName、categoryAlias | 无 |
| GET /category | 无 | 无 | 无 |
| GET /category/detail | query | id | 无 |
| PUT /category | JSON | id、categoryName、categoryAlias | 无 |
| DELETE /category | query | id | 无 |
| POST /article | JSON | title、content、coverImg、state、categoryId | 无 |
| GET /article | query | pageNum、pageSize | categoryId、state |
| GET /article/detail | query | id | 无 |
| PUT /article | JSON | id、title、content、coverImg、state、categoryId | 无 |
| DELETE /article | query | id | 无 |
| POST /upload | multipart/form-data | file | 无 |

文章 title 为 1～10 非空白字符；coverImg 必须是 URL；state 为“草稿”或“已发布”；categoryId 是数字。
列表 data 为 {total, items}，total 是当前账号及查询条件下的记录数，不是数据库整表总数。

## 本次发现与修复

- 新增笔记原来漏传 coverImg：数据库拒绝插入，不能当作保存成功。现已提供 URL 输入与上传；编辑也校验并提交封面。
- 上传必须传 FormData 的 file 字段，不能用 JSON，也不能手动覆盖 multipart boundary。
- 筛选没有匹配与账号完全没有笔记现在分别提示，展示账号总量与当前匹配量。
- 新增成功后清空筛选、回第一页，按创建时间及 ID 倒序显示；失败保留表单内容。
- 动态 SQL 中的 # 注释改为 XML 注释，空筛选参数不再作为过滤条件。
- 后端必填校验拦截缺失 coverImg/state；数据库堆栈不再直接显示在网页。

## 文档与现有其他实现差异（不冒充已完成）

- 用户头像实现使用 avatarurl，而文档为 avatarUrl，需要接入用户资料页面时统一。
- 当前 CategoryController 尚无 /category/detail；已有分类列表足以供笔记选分类，但不等于详情接口已实现。
- 文档中的 PUT /article 原项目缺失，前一次整合已补齐。

数据库历史笔记具体属于哪个账号尚未核实，不能仅凭截图认定丢失；不应移除 create_user 条件向其他账号暴露数据。
