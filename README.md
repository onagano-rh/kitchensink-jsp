---
title: Using External Database
author: NAGANO Osamu, Red Hat
description: 2024-01-17
paginate: true
headingDivider: 2
---

# 概要

## はじめに (1/2)

[EAP 7.4のQuickstarts](https://github.com/jboss-developer/jboss-eap-quickstarts/tree/7.4.x) 内には
"kitchensink"で始まる複数のプロジェクトがある。
これはDBを使う最小限のアプリとして用意されたもので、使用する要素技術に応じて若干のバリエーションがある。

ここではビューに枯れた要素技術であるJSPを用いた"kitchensink-jsp"を例に採用して話を進める。

"kitchensink-jsp"に限らず、どのQuickstartsプロジェクトもDBを使うものは内臓のH2 DBを使うようになっている。
内臓であるため、JDBCドライバのインストールやデータソースの登録といった作業を行うことなく、`mvn wildfly:deploy`
とするだけで動かすことができる。

## はじめに (2/2)

本番環境ではより本格的なDBを使うのが常であるため、自分でそのDBを外部に用意し、EAPにも適切な設定を行う必要がある。
ここでは外部DBとして PostgreSQL を例にとり、ローカルマシン上(Dockerを使う)とOpenShift上でそれぞれ動かしてみる。
PostgreSQL以外の他のDBでも基本的な流れは同様である。

動かすだけなら `$QUICKSTART_HOME/kitchensink-jsp` 内で直接 `mvn wildfly:deploy` するだけでよいが、
今後の扱いやすさのためにも `kitchenshink-jsp` のみを取り出した独立のプロジェクトとする。

# ローカルマシン上で内臓DBをそのまま使って動かす

## kitchensink-jspのみを含むディレクトリを作成

変数 QUICKSTART_HOME が指すディレクトリに、Quickstarts全体をすでにクローン済みとする。

```shell
cd ~/work # 任意の作業用ディレクトリを作成

cp -a $QUICKSTART_HOME/kitchensink-jsp .
cd kitchensink-jsp
cp $QUICKSTART_HOME/pom.xml pom-parent.xml
vi pom.xml # "../pom.xml"を"pom-parent.xml"に書き換える
```

エディタはviでなくともVS Codeでよい。
すなわち `code ~/work/kitchensink-jsp` で起動し、以降はVS Code内のターミナルで作業してもよい。

## 不要なファイルの削除

```shell
rm README.html
rm crw-java8-maven-eap.yaml
rm crw-java11-maven-eap.yaml
```

## 実行

別のターミナルでEAPを起動しておく。

```shell
cd $JBOSS_HOME
./bin/standalone.sh
```

プロジェクト内のディレクトリで`mvn`コマンドによりビルドとデプロイを行う。

```shell
mvn package # 下記の行だけでもビルドは行われる
mvn wildfly:deploy
```

<http://localhost:8080/kitchensink-jsp/> にブラウザでアクセスしアプリを操作してみる。

# DockerでPostgreSQLを起動しそれを利用する

## コンテナの起動

Docker Hubにある [公式イメージ](https://hub.docker.com/_/postgres) を利用する。
使い方もこのページに書いてある。

```shell
docker run -d --name mypgserver  -p 5432:5432 \
  -e POSTGRES_USER=pguser -e POSTGRES_PASSWORD=pgpassword -e POSTGRES_DB=pgdatabase \
  docker.io/library/postgres:15
```

`docker.io/library/postgres` はイメージのフルネームであり、Docker Hubに限っては `postgres` だけでも動作する。

## コンテナのその他の操作

```shell
docker stop mypgserver # 停止
docker start mypgserver # 再起動

# psqlコマンドで接続
docker exec -it mypgserver psql -U pguser pgdatabase

dokcer rm mypgserver # (停止後に)削除
```

## 参考: IBM DB2の場合

[Docker Hubのページ](https://hub.docker.com/r/ibmcom/db2)

```shell
docker run -d --name mydb2 --privileged=true -p 50000:50000 \
  -e LICENSE=accept -e DB2INST1_PASSWORD=admin -e DBNAME=testdb \
  docker.io/ibmcom/db2:11.5.8.0
```

ユーザ名は `db2inst1` になる。

`docker exec -ti mydb2 bash -c "su - db2inst1"` で接続する。

## JDBCドライバのインストール

```shell
# Maven Centralより ~/.m2 以下にダウンロード
mvn dependency:get -Dartifact=org.postgresql:postgresql:42.7.1

# EAP内にJDBCドライバのモジュールを作成
cd $JBOSS_HOME
./bin/jboss-cli.sh                                         
[disconnected /] module add --name=org.postgresql \
  --resources=~/.m2/repository/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar \
  --dependencies=javaee.api,sun.jdk,ibm.jdk,javax.api,javax.transaction.api
[disconnected /] exit
```

JBoss CLI内の `module add` のコマンドは、DBごとに [開発ガイド][devguide] に例が用意されているので覚える必要はない。

[devguide]: https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/html/configuration_guide/datasource_management#example_postgresql_xa_datasource

## アプリのアンデプロイ

既存のデータソースをいったん削除するためにアンデプロイする。

```shell
cd ~/work/kitchensink-jsp
mvn wildfly:undeploy # 一旦アンデプロイする
```

## サーバにドライバとデータソースを登録

EAPが起動済みの状態で行う必要がある。

```shell
$JBOSS_HOME/bin/jboss-cli.sh -c

# ドライバの登録
[standalone@localhost:9990 /] /subsystem=datasources/jdbc-driver=postgresql:add( \
  driver-name=postgresql,driver-module-name=org.postgresql, \
  driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource)

# データソースの登録（JNDI名、ユーザ名やパスワード等を実際のものに合わせること）
[standalone@localhost:9990 /] xa-data-source add --name=KitchensinkJSPQuickstartDS \
  --jndi-name=java:jboss/datasources/KitchensinkJSPQuickstartDS --driver-name=postgresql \
  --user-name=pguser --password=pgpassword --validate-on-match=true --background-validation=false \
  --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker \
  --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter \
  --xa-datasource-properties={"ServerName"=>"localhost","PortNumber"=>"5432","DatabaseName"=>"pgdatabase"}
```

ユーザ名やパスワード、DB名はDockerでのコンテナ作成時に自分で指定したものに合わせる。
JNDI名はソースコード内の ./src/main/resources/META-INF/persistence.xml に書いてある。

これも [開発ガイド][devguide] にコマンド例があるので覚える必要はない。

## 外部DBを利用する形で起動する

```shell
cd ~/work/kitchensink-jsp

# 内臓DBを使ったデータソースの定義を削除する
rm src/main/webapp/WEB-INF/kitchensink-quickstart-ds.xml

# 一旦cleanして再デプロイ
mvn clean wildfly:deploy
```

<http://localhost:8080/kitchensink-jsp/> にアクセスし試しにデータを入力してみる。

`psql` コマンドで `SELECT * FROM memberjsp;` を実行してみて入力したデータの存在を確認してみる。

# OpenShift上でPostgreSQLとEAPを動かす

## プロジェクトの作成

OpenShiftにログインし自分のプロジェクト(以降 `$MYPROJ` で参照)を作成する。

```shell
oc login -u <自分のユーザ名> https://api.<OpenShiftクラスタ>:6443
oc new-project <一意なプロジェクト名>

# プロジェクト名を変数MYPROJに保存
MYPROJ=$(oc project -q)

source <(oc completion bash)
oc status
oc whoami
```

## EAP公式のImageStreamとTemplateのインポート

```shell
# ImageStreamのインポート
oc apply -f https://raw.githubusercontent.com/jboss-container-images/jboss-eap-openshift-templates/eap74/eap74-openjdk17-image-stream.json

# Templateのインポート
for resource in eap74-amq-persistent-s2i.json eap74-amq-s2i.json eap74-basic-s2i.json eap74-https-s2i.json eap74-sso-s2i.json ; \
  do oc apply -f https://raw.githubusercontent.com/jboss-container-images/jboss-eap-openshift-templates/eap74/templates/${resource}; done
```

## S2Iビルドにおけるカスタマイズのためのファイル群の作成

JDBCドライバのインストールやデータソースの設定のために、プロジェクト直下に以下のディレクトリを作成し必要なファイルを配置してビルドプロセスをカスタマイズする。

- `.s2i`
- `extensions`
- `modules`

このカスタマイズに関する公式ドキュメントは以下のとおり。

- [第4章 Java アプリケーションに対して JBoss EAP for OpenShift イメージを設定](https://access.redhat.com/documentation/ja-jp/red_hat_jboss_enterprise_application_platform/7.4/html/getting_started_with_jboss_eap_for_openshift_container_platform/configuring_eap_openshift_image)
- [第8章 参考情報, 8.13. S2I](https://access.redhat.com/documentation/ja-jp/red_hat_jboss_enterprise_application_platform/7.4/html/getting_started_with_jboss_eap_for_openshift_container_platform/reference_s2i)

## .s2i の作成

```shell
cd ~/work/kitchensink-jsp

mkdir .s2i
vi .s2i/environment
```

以下の内容を持つテキストファイル `.s2i/environment` を作成する。

```
CUSTOM_INSTALL_DIRECTORIES=extensions
```

## extensions の作成

```shell
mkdir extensions
```

以下の3つのファイルを作成する。

- `extensions/install.sh`
- `extensions/postconfigure.sh`
- `extensions/config-database.cli`

## extensions/install.sh の作成

以下の内容で作成する。

```
#!/usr/bin/env bash
set -x
echo "=== Running $PWD/install.sh"
injected_dir=$1
# copy any needed files into the target build.
cp -rf ${injected_dir} $JBOSS_HOME/extensions
```

## extensions/postconfigure.sh の作成

以下の内容で作成する。

```
#!/usr/bin/env bash
echo "=== Executing postconfigure.sh"
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/extensions/config-database.cli
```

## extensions/config-database.cli の作成

以下の内容で作成する。

```
embed-server --std-out=echo  --server-config=standalone-openshift.xml

# Register PostgreSQL JDBC driver

/subsystem=datasources/jdbc-driver=postgresql:add( \
  driver-name=postgresql, \
  driver-module-name=org.postgresql, \
  driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource )

# Create a datasource named KitchensinkJSPQuickstartDS

xa-data-source add \
  --name=KitchensinkJSPQuickstartDS \
  --jndi-name=java:jboss/datasources/KitchensinkJSPQuickstartDS \
  --driver-name=postgresql \
  --user-name=${env.MYDB_USERNAME} \
  --password=${env.MYDB_PASSWORD} \
  --validate-on-match=true \
  --background-validation=false \
  --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker \
  --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter \
  --xa-datasource-properties={ \
    "ServerName"=>"${env.MYDB_SERVER}", \
    "PortNumber"=>"${env.MYDB_PORT}", \
    "DatabaseName"=>"${env.MYDB_DATABASE}" }

quit
```

## modules の作成

ローカルのEAPにはPostgreSQLのJDBCドライバをモジュールとしてインストールしているはずなので、それをコピーしてくる。

```shell
mkdir modules
cp -a $JBOSS_HOME/modules/org modules/
```

（公式ドキュメントから`module add`のコマンド例をコピペした場合は`org`ではなく`com`になっている可能性あり。）
以下の階層で二つのファイルが存在するはず。

```
modules/org/postgresql/main/postgresql-42.7.1.jar
modules/org/postgresql/main/module.xml
```

## OpenShift上でPostgreSQLを起動する

`openshift`プロジェクトに`postgresql-persistent`というTemplateが用意されているのでそれを使って起動する。

```shell
# Templateの詳細の確認
oc describe template postgresql-persistent -n openshift

# 必要なパラメタを指定してTemplateを適用する
oc new-app --template=postgresql-persistent \
  -p POSTGRESQL_VERSION=15-el8 -p POSTGRESQL_USER=pguser \
  -p POSTGRESQL_PASSWORD=pgpassword -p POSTGRESQL_DATABASE=pgdatabase
```

そのうちPostgreSQLのポッドが起動するので `oc get pod` でポッド名を確認し `oc logs -f postgresql-1-XXXXX` でログを見たりしてみる。

イメージが取得できないなどトラブルシュートには ` oc get events --sort-by='.lastTimestamp'` といったコマンドも使える。

## GitHubにリポジトリを作成

GitHubに自分のアカウントでログインし、右上のプラスボタンから空のリポジトリを "kitchensink-jsp" の名前で作成する。

ここでは参考例として **プライベートリポジトリとして** 作成してみる。

リポジトリ作成後に表示されるインストラクションに従ってローカルマシン上の kitchensink-jsp をGitリポジトリとして初期化しGitHubにプッシュする。

## kitchensink-jsp をGitHubに置く

```shell
cd ~/work/kitchensink-jsp

# ビルドの成果物が格納されるtargetディレクトリは無視するように設定
echo "target" >> .gitignore

git init
git branch -m main
git remote add origin git@github.com:<自分のアカウント名>/kitchensink-jsp.git

git add .
git commit -m "initial commit"

git push -u origin main
```

## ソースコード取得用のSecretの作成

リポジトリをプライベートで作成したのでそのままでは取得に失敗する。
後の`oc new-app`で使用するために自分のSSH秘密鍵をSecretとして作成しておく。

```shell
oc create secret generic my-github-key \
  --from-file=ssh-privatekey=${HOME}/.ssh/id_rsa --type=kubernetes.io/ssh-auth
```

## アプリのビルドとデプロイ

```shell
oc new-app --template=eap74-basic-s2i \
  -p APPLICATION_NAME=myapp  \
  -p IMAGE_STREAM_NAMESPACE=$(oc project -q) \
  -p EAP_IMAGE_NAME=jboss-eap74-openjdk17-openshift:latest \
  -p EAP_RUNTIME_IMAGE_NAME=jboss-eap74-openjdk17-runtime-openshift:latest \
  -p SOURCE_REPOSITORY_URL=git@github.com:<自分のアカウント名>/kitchensink-jsp.git \
  -p SOURCE_REPOSITORY_REF=main \
  -p CONTEXT_DIR="" \
  --source-secret=my-github-key \
  -e MYDB_USERNAME=pguser \
  -e MYDB_PASSWORD=pgpassword \
  -e MYDB_DATABASE=pgdatabase \
  -e MYDB_SERVER=postgresql \
  -e MYDB_PORT=5432
```

`MYDB_` で始まるDB接続情報は自分で作成したPostgreSQLのものに合わせる。

## 様々なocコマンド

```shell
# ビルドの様子を確認
oc logs -f bc/myapp-build-artifacts

# (コードを編集してpush後に)ビルドを再開
oc start-build myapp --follow --incremental

# oc new-appで作成されたリソースを全て削除
oc delete all -l application=myapp
```

## 動作確認

```shell
# Routeの確認 ("https://"を付けてブラウザでアクセス)
oc get route

# DBのデータの確認 (実際のポッド名は `oc get pod` で確認)
oc rsh postgresql-1-XXXXX psql -U pguser pgdatabase
```

