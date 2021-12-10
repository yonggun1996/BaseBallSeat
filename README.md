# 야구장 좌석 리뷰 앱 프로젝트

## 이 프로젝트는 명지전문대학교 <스마트 앱 프로그래밍> 기말 과제물 입니다.

### 간단한 프로젝트 설명

  - 각 구장별로 특정 좌석에 리뷰를 올릴 수 있는 커뮤니티 앱 입니다.
  - 주로 FireBase에서 제공하는 기능을 토대로 프로젝트를 진행했습니다.
  - AndroidStudio를 이용해 만들었고, 언어는 Kotlin을 사용했습니다.

### 인증을 구현했네요? 어떻게 했나요?

  - Firebase에서 제공하는 인증 기능을 토대로 Google과 Facebook으로 인증을 했습니다.
  - 기본적으로 로그인이 되어야 글을 읽고, 추가할 수 있게 구현햇습니다.
  - Firebase의 공식 문서를 통해서 해결했습니다.
  - Link : https://firebase.google.com/docs/auth
  
### FireStorage를 이용한 이유가 있을까요?

  - 기존에 이미지의 Bitmap을 String으로 저장을 하는 방식을 사용했는데 이미지를 업로드 하는 과정에서 메모리 초과를 일으켰습니다.
  - 그래서 대용량 저장소를 통해서 이미지를 저장하고, 그에 대한 경로를 통해 Glide 라이브러리를 사용해 이미지를 띄우게 한 결과 메모리 초과는 일어나지 않았습니다.
  - 또한 여러 기기에서 이미지를 업로드 하기 때문에 통합된 저장소를 통해서 다른 기기에 올린 이미지도 보일 수 있도록 해야하기에 사용했습니다.
  - Link : https://firebase.google.com/docs/storage/android/start
  
### Cloud Firestore를 이용한 이유가 있을까요?

  - 처음에는 RealtimeDatabase를 사용했는데 특정 데이터를 조건에 따라서 출력할 수 없어서 다른 방법을 택했습니다.(날짜 역순으로 보여져야 했기 때문에 쿼리 기능이 갖춰진 DB가 필요했습니다.)
  - Cloud Firestore는 조건에 맞게 데이터를 보여지게 할 수 있어서 사용하게 되었습니다.
  - 기본적인 Cloud Firestore 이용 방법 : https://firebase.google.com/docs/firestore/quickstart
  - 또한 데이터를 전부 호출하는 것이 아닌 특정 개수만큼 호출해 사용하게 했습니다.
  - 알게 된 링크 : (구글링 키워드 : firestore paging)
    https://www.youtube.com/watch?v=HQgJvHXsNOQ

### Camera Permission을 사용하게 됐네요?
  
  - 카메라로 사진을 찍어 이미지뷰에 미리보기를 해야하는 로직을 짜야 했습니다.
  - 또한 기본적으로 게시글 내용중 이미지가 올라가야 하기 때문에 카메라 권한을 묻는 알고리즘이 필요했습니다.
  - 도움이 된 유튜브 영상 : https://www.youtube.com/watch?v=inprJiLDUIU&t=1986s
