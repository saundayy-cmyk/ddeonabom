/* 떠나봄 — Tailwind 토큰 설정 (CDN용)*/
tailwind.config = {
  theme: {
    extend: {
      colors: {
        ink:   '#14161C',   // 본문 텍스트
        muted: '#5C6373',   // 보조 텍스트
        faint: '#9AA0AE',   // 흐린 텍스트
        line:  '#E7E9EF',   // 경계선
        sunk:  '#F4F5F8',   // 가라앉은 배경
        deep:  '#0E1116',   // 다크 섹션
        brand: { DEFAULT: '#1F47E6', ink: '#143BBE', soft: '#EBEEFD', light: '#8AA0FF' }, // 코발트 포인트
        ok:    '#1F8A5B',
        warn:  '#B2772A',
        err:   '#D23B33',
      },
      fontFamily: {
        sans: ['"Noto Sans KR"', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        '2xl': '18px',   // 큰 카드 (r-lg)
      },
      boxShadow: {
        soft: '0 1px 2px rgba(20,22,28,0.04), 0 1px 3px rgba(20,22,28,0.06)',
        card: '0 2px 6px rgba(20,22,28,0.05), 0 8px 24px rgba(20,22,28,0.07)',
        pop:  '0 8px 16px rgba(20,22,28,0.06), 0 24px 48px rgba(20,22,28,0.10)',
      },
      spacing: {
        header: '68px',
        '6.5': '26px',   // 순위 배지
        '7.5': '30px',   // 소형 칩
        '8.5': '34px',   // 칩 (질문 게시판)
        '9.5': '38px',   // 작성자 아바타
        '10.5': '42px',  // 댓글 아바타
        '13': '52px',    // 기능 아이콘 박스
        '18': '72px',    // 후기 사진 썸네일
        '22': '88px',    // 마이페이지 아바타
        '70': '280px',   // 지도 카드
      },
      minHeight: {
        feature: '270px',
        authshell: 'calc(100vh - 68px)',
        photo: '460px',
        composer: '104px',
      },
      minWidth: {
        date: '150px',
      },
      aspectRatio: {
        portrait: '5 / 6',
        card: '4 / 5',
        photo: '4 / 3',
      },
      gridTemplateColumns: {
        hero: '1.05fr 0.95fr',
        mypage: '220px 1fr',
        detail: '1.4fr 1fr',
        infotable: '120px 1fr',
        plan: '460px 1fr',
        qarow: '1fr 130px 110px 80px',
        admin: '1.6fr 1fr',
      },
      height: {
        plan: 'calc(100vh - 68px)',
      },
      maxWidth: {
        site: '1200px',
        wide: '1320px',
        board: '1000px',
        post: '820px',
        postwide: '920px',
        index: '1240px',
        vw80: '80vw',
      },
      width: {
        errmap: '340px',
      },
      fontSize: {
        '11':    ['11px', '1.4'],
        '13':    ['13px', '1.5'],
        '15':    ['15px', '1.5'],
        '17':    ['17px', '1.4'],
        '22':    ['22px', '1.2'],
        '28':    ['28px', '1.3'],
        '32':    ['32px', '1.2'],
        '56':    ['56px', { lineHeight: '1', letterSpacing: '-0.045em' }],
        display: ['76px', { lineHeight: '1.04', letterSpacing: '-0.045em' }],
        hero:    ['60px', { lineHeight: '1.05', letterSpacing: '-0.045em' }],
        h1:      ['44px', { lineHeight: '1.12', letterSpacing: '-0.03em' }],
        h2:      ['36px', { lineHeight: '1.1',  letterSpacing: '-0.03em' }],
        quote:   ['52px', { lineHeight: '1.28', letterSpacing: '-0.03em' }],
      },
    },
  },
};
