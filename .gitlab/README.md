# Git 컨벤션
 #### ⚠️형식에서 대소문자 구분을 유의해주세요!!!
## 1. Issue 등록
Jira에 담당할 작업에서 개발해야할 내용들을 issue 템플릿 형식에 맞게 등록한다.
### Issue 제목 형식
> [Part/Type] 이슈 제목
- (예시) [Android/Feat] 기능 개발

<br>

## 2. branch 생성
생성한 이슈의 내용만을 개발할 branch를 이름 형식에 맞춰 생성한다.
해당 기능 개발이 끝나면 master branch로 Merge Request를 한다.
### branch 이름 형식
> type/#issue_number

- (예시) feat/#3

<br>

## 3. commit 등록
변경 사항을 commit 할 때, 제목(title) 형식을 맞춰 commit한다.

commit은 issue 등록할 때 to-do 리스트에 작성한 내용마다 하는 것을 추천...
### commit 제목 형식
> branch이름 : commit 제목

- (예시) feat/#3 : example 기능 구현

<br>

## 4. Merge Request 등록
작업이 끝난 issue에 해당하는 branch를 master branch와 merge하기 위해, Merge Request를 MR template 형식에 맞춰 작성하고 요청한다.
### MR 제목 형식
> [Part/Type] MR 제목

- (예시) [BE/Fix] 기능 수정