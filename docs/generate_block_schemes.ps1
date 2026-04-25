param(
    [string]$OutputPath = "docs\block-schemes.png"
)

Add-Type -AssemblyName System.Drawing

$width = 3200
$height = 5120

$bitmap = [System.Drawing.Bitmap]::new($width, $height)
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$graphics.Clear([System.Drawing.Color]::White)

$pen = [System.Drawing.Pen]::new([System.Drawing.Color]::Black, 3)
$framePen = [System.Drawing.Pen]::new([System.Drawing.Color]::Black, 2)
$brush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::Black)
$whiteBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)

$titleFont = [System.Drawing.Font]::new("Arial", 30, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
$shapeFont = [System.Drawing.Font]::new("Arial", 17, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
$smallFont = [System.Drawing.Font]::new("Arial", 15, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)

$centerFormat = [System.Drawing.StringFormat]::new()
$centerFormat.Alignment = [System.Drawing.StringAlignment]::Center
$centerFormat.LineAlignment = [System.Drawing.StringAlignment]::Center

function New-Node {
    param(
        [int]$X,
        [int]$Y,
        [int]$W,
        [int]$H
    )

    [pscustomobject]@{
        X = [single]$X
        Y = [single]$Y
        W = [single]$W
        H = [single]$H
        CX = [single]($X + $W / 2)
        CY = [single]($Y + $H / 2)
        TopX = [single]($X + $W / 2)
        TopY = [single]$Y
        BottomX = [single]($X + $W / 2)
        BottomY = [single]($Y + $H)
        LeftX = [single]$X
        LeftY = [single]($Y + $H / 2)
        RightX = [single]($X + $W)
        RightY = [single]($Y + $H / 2)
    }
}

function Pt {
    param([double]$X, [double]$Y)
    [System.Drawing.PointF]::new([single]$X, [single]$Y)
}

function Draw-Text {
    param(
        [string]$Text,
        [double]$X,
        [double]$Y,
        [double]$W,
        [double]$H,
        [System.Drawing.Font]$Font = $shapeFont
    )

    $rect = [System.Drawing.RectangleF]::new([single]$X, [single]$Y, [single]$W, [single]$H)
    $graphics.DrawString($Text, $Font, $brush, $rect, $centerFormat)
}

function Draw-Label {
    param(
        [string]$Text,
        [double]$X,
        [double]$Y
    )

    $rect = [System.Drawing.RectangleF]::new([single]($X - 40), [single]($Y - 16), 80, 32)
    $graphics.FillRectangle($whiteBrush, $rect)
    $graphics.DrawString($Text, $smallFont, $brush, $rect, $centerFormat)
}

function Draw-Terminator {
    param([object]$Node, [string]$Text)
    $graphics.DrawEllipse($pen, $Node.X, $Node.Y, $Node.W, $Node.H)
    Draw-Text $Text ($Node.X + 12) ($Node.Y + 8) ($Node.W - 24) ($Node.H - 16)
}

function Draw-Process {
    param([object]$Node, [string]$Text)
    $graphics.DrawRectangle($pen, $Node.X, $Node.Y, $Node.W, $Node.H)
    Draw-Text $Text ($Node.X + 10) ($Node.Y + 6) ($Node.W - 20) ($Node.H - 12)
}

function Draw-Predefined {
    param([object]$Node, [string]$Text)
    $graphics.DrawRectangle($pen, $Node.X, $Node.Y, $Node.W, $Node.H)
    $graphics.DrawLine($pen, $Node.X + 18, $Node.Y, $Node.X + 18, $Node.Y + $Node.H)
    $graphics.DrawLine($pen, $Node.X + $Node.W - 18, $Node.Y, $Node.X + $Node.W - 18, $Node.Y + $Node.H)
    Draw-Text $Text ($Node.X + 24) ($Node.Y + 6) ($Node.W - 48) ($Node.H - 12)
}

function Draw-Input {
    param([object]$Node, [string]$Text)
    $points = [System.Drawing.PointF[]]@(
        (Pt ($Node.X + 24) $Node.Y),
        (Pt ($Node.X + $Node.W) $Node.Y),
        (Pt ($Node.X + $Node.W - 24) ($Node.Y + $Node.H)),
        (Pt $Node.X ($Node.Y + $Node.H))
    )
    $graphics.DrawPolygon($pen, $points)
    Draw-Text $Text ($Node.X + 22) ($Node.Y + 6) ($Node.W - 44) ($Node.H - 12)
}

function Draw-Decision {
    param([object]$Node, [string]$Text)
    $points = [System.Drawing.PointF[]]@(
        (Pt $Node.CX $Node.Y),
        (Pt ($Node.X + $Node.W) $Node.CY),
        (Pt $Node.CX ($Node.Y + $Node.H)),
        (Pt $Node.X $Node.CY)
    )
    $graphics.DrawPolygon($pen, $points)
    Draw-Text $Text ($Node.X + 32) ($Node.Y + 28) ($Node.W - 64) ($Node.H - 56)
}

function Draw-Line {
    param([double]$X1, [double]$Y1, [double]$X2, [double]$Y2)
    $graphics.DrawLine($pen, [single]$X1, [single]$Y1, [single]$X2, [single]$Y2)
}

function Draw-ArrowHead {
    param([double]$X1, [double]$Y1, [double]$X2, [double]$Y2)
    $angle = [Math]::Atan2($Y2 - $Y1, $X2 - $X1)
    $length = 16.0
    $wing = 8.0
    $p1 = Pt (
        $X2 - $length * [Math]::Cos($angle) + $wing * [Math]::Sin($angle)
    ) (
        $Y2 - $length * [Math]::Sin($angle) - $wing * [Math]::Cos($angle)
    )
    $p2 = Pt (
        $X2 - $length * [Math]::Cos($angle) - $wing * [Math]::Sin($angle)
    ) (
        $Y2 - $length * [Math]::Sin($angle) + $wing * [Math]::Cos($angle)
    )
    $tip = Pt $X2 $Y2
    $graphics.FillPolygon($brush, [System.Drawing.PointF[]]@($tip, $p1, $p2))
}

function Draw-Arrow {
    param([double]$X1, [double]$Y1, [double]$X2, [double]$Y2)
    Draw-Line $X1 $Y1 $X2 $Y2
    Draw-ArrowHead $X1 $Y1 $X2 $Y2
}

function Draw-Section {
    param(
        [double]$X,
        [double]$Y,
        [double]$W,
        [double]$H,
        [string]$Title
    )

    $graphics.DrawRectangle($framePen, [single]$X, [single]$Y, [single]$W, [single]$H)
    Draw-Text $Title ($X + 20) ($Y + 18) ($W - 40) 42 $titleFont
}

Draw-Section 40 70 3120 1960 "Блок-схема работы программы"
Draw-Section 40 2080 1500 2960 "Блок-схема регистрации пользователя"
Draw-Section 1660 2080 1500 2960 "Блок-схема подачи заявки на задачу"

# Section 1
$A = New-Node 120 140 220 80
$B = New-Node 100 270 260 90
$C = New-Node 100 410 260 90
$D = New-Node 100 570 260 180
$E = New-Node 90 830 280 90
$F = New-Node 90 970 280 90
$G = New-Node 100 1130 260 180
$H = New-Node 920 570 260 180
$J = New-Node 1220 880 300 90
$J1 = New-Node 1180 1060 380 280
$L = New-Node 1720 880 300 90
$L1 = New-Node 1680 1060 380 340
$N = New-Node 2220 880 340 90
$N1 = New-Node 2200 1060 380 280
$T = New-Node 1710 1530 280 180
$W = New-Node 1710 1800 280 90
$X = New-Node 1740 1940 220 80

Draw-Terminator $A "Начало"
Draw-Input $B "Запуск веб-приложения"
Draw-Process $C "Главная страница"
Draw-Decision $D "Пользователь`nавторизован?"
Draw-Input $E "Форма входа /`nрегистрации"
Draw-Predefined $F "Проверка`nданных"
Draw-Decision $G "Вход`nвыполнен?"
Draw-Decision $H "Определить`nроль"
Draw-Process $J "Режим`nволонтёра"
Draw-Process $J1 "Разделы:`n- Задачи`n- Профиль и навыки`n- Мои заявки"
Draw-Process $L "Режим`nорганизатора"
Draw-Process $L1 "Разделы:`n- Управление задачами`n- Заявки волонтёров`n- Матчинг`n- Профиль"
Draw-Process $N "Режим координатора /`nадмина"
Draw-Process $N1 "Разделы:`n- Панель координатора`n- Матчинг`n- Просмотр профилей"
Draw-Decision $T "Выход из`nаккаунта?"
Draw-Predefined $W "Выход из`nсистемы"
Draw-Terminator $X "Конец"

Draw-Arrow $A.BottomX $A.BottomY $B.TopX $B.TopY
Draw-Arrow $B.BottomX $B.BottomY $C.TopX $C.TopY
Draw-Arrow $C.BottomX $C.BottomY $D.TopX $D.TopY

Draw-Arrow $D.BottomX $D.BottomY $E.TopX $E.TopY
Draw-Label "Нет" 300 790

Draw-Arrow $E.BottomX $E.BottomY $F.TopX $F.TopY
Draw-Arrow $F.BottomX $F.BottomY $G.TopX $G.TopY

Draw-Line $D.RightX $D.RightY 920 $D.RightY
Draw-Arrow 920 $D.RightY $H.LeftX $H.LeftY
Draw-Label "Да" 640 615

Draw-Line $G.RightX $G.RightY 600 $G.RightY
Draw-Line 600 $G.RightY 600 $H.LeftY
Draw-Arrow 600 $H.LeftY $H.LeftX $H.LeftY
Draw-Label "Да" 580 910

Draw-Line $G.LeftX $G.LeftY 40 $G.LeftY
Draw-Line 40 $G.LeftY 40 $E.CY
Draw-Arrow 40 $E.CY $E.LeftX $E.LeftY
Draw-Label "Нет" 70 990

Draw-Line $H.BottomX $H.BottomY $H.BottomX 810
Draw-Line $H.BottomX 810 2390 810

Draw-Arrow 1370 810 $J.TopX $J.TopY
Draw-Arrow 1870 810 $L.TopX $L.TopY
Draw-Arrow 2390 810 $N.TopX $N.TopY
Draw-Label "VOL" 1370 785
Draw-Label "ORG" 1870 785
Draw-Label "COORD" 2390 785

Draw-Arrow $J.BottomX $J.BottomY $J1.TopX $J1.TopY
Draw-Arrow $L.BottomX $L.BottomY $L1.TopX $L1.TopY
Draw-Arrow $N.BottomX $N.BottomY $N1.TopX $N1.TopY

Draw-Line $J1.BottomX $J1.BottomY $J1.BottomX 1490
Draw-Line $J1.BottomX 1490 $T.TopX 1490
Draw-Arrow $T.TopX 1490 $T.TopX $T.TopY

Draw-Arrow $L1.BottomX $L1.BottomY $T.TopX $T.TopY

Draw-Line $N1.BottomX $N1.BottomY $N1.BottomX 1490
Draw-Line $N1.BottomX 1490 $T.TopX 1490

Draw-Arrow $T.BottomX $T.BottomY $W.TopX $W.TopY
Draw-Label "Да" 2010 1910

Draw-Line $T.LeftX $T.LeftY 1050 $T.LeftY
Draw-Line 1050 $T.LeftY 1050 810
Draw-Arrow 1050 810 1050 $H.BottomY
Draw-Label "Нет" 1400 1765

Draw-Arrow $W.BottomX $W.BottomY $X.TopX $X.TopY

# Section 2
$R1 = New-Node 690 2150 220 80
$R2 = New-Node 650 2280 300 90
$R3 = New-Node 650 2410 300 90
$R4 = New-Node 650 2540 300 90
$R5 = New-Node 650 2680 300 180
$Rerr1 = New-Node 220 2730 280 90
$R6 = New-Node 650 2900 300 90
$R7 = New-Node 650 3040 300 180
$Rerr2 = New-Node 220 3090 280 90
$R8 = New-Node 650 3260 300 90
$R9 = New-Node 650 3400 300 180
$Rerr3 = New-Node 220 3450 280 90
$R10 = New-Node 650 3620 300 90
$R11 = New-Node 650 3750 300 90
$R12 = New-Node 650 3880 300 90
$R13 = New-Node 650 4020 300 180
$R14 = New-Node 650 4250 300 90
$R15 = New-Node 1040 4250 300 90
$R16 = New-Node 850 4390 300 90
$R17 = New-Node 850 4520 300 90
$R18 = New-Node 850 4650 300 90
$R19 = New-Node 890 4780 220 80

Draw-Terminator $R1 "Начало"
Draw-Input $R2 "Открытие формы`nрегистрации"
Draw-Input $R3 "Ввод логина, email,`nпароля и роли"
Draw-Predefined $R4 "Проверка`nзаполнения полей"
Draw-Decision $R5 "Все поля`nзаполнены`nкорректно?"
Draw-Input $Rerr1 "Ошибка`nзаполнения"
Draw-Predefined $R6 "Проверка`nуникальности логина"
Draw-Decision $R7 "Логин`nсвободен?"
Draw-Input $Rerr2 "Логин уже`nзанят"
Draw-Predefined $R8 "Проверка`nуникальности email"
Draw-Decision $R9 "Email`nсвободен?"
Draw-Input $Rerr3 "Email уже`nзарегистрирован"
Draw-Process $R10 "Определение роли"
Draw-Process $R11 "Хеширование пароля"
Draw-Process $R12 "Создание`nучётной записи"
Draw-Decision $R13 "Роль =`nVOLUNTEER?"
Draw-Process $R14 "Создание профиля`nволонтёра"
Draw-Process $R15 "Без профиля`nволонтёра"
Draw-Process $R16 "Генерация`nJWT-токена"
Draw-Process $R17 "Сохранение данных`nавторизации"
Draw-Input $R18 "Переход на`nглавную страницу"
Draw-Terminator $R19 "Конец"

Draw-Arrow $R1.BottomX $R1.BottomY $R2.TopX $R2.TopY
Draw-Arrow $R2.BottomX $R2.BottomY $R3.TopX $R3.TopY
Draw-Arrow $R3.BottomX $R3.BottomY $R4.TopX $R4.TopY
Draw-Arrow $R4.BottomX $R4.BottomY $R5.TopX $R5.TopY

Draw-Arrow $R5.BottomX $R5.BottomY $R6.TopX $R6.TopY
Draw-Label "Да" 980 2870
Draw-Arrow $R6.BottomX $R6.BottomY $R7.TopX $R7.TopY

Draw-Arrow $R7.BottomX $R7.BottomY $R8.TopX $R8.TopY
Draw-Label "Да" 980 3230
Draw-Arrow $R8.BottomX $R8.BottomY $R9.TopX $R9.TopY

Draw-Arrow $R9.BottomX $R9.BottomY $R10.TopX $R10.TopY
Draw-Label "Да" 980 3590
Draw-Arrow $R10.BottomX $R10.BottomY $R11.TopX $R11.TopY
Draw-Arrow $R11.BottomX $R11.BottomY $R12.TopX $R12.TopY
Draw-Arrow $R12.BottomX $R12.BottomY $R13.TopX $R13.TopY

Draw-Arrow $R13.BottomX $R13.BottomY $R14.TopX $R14.TopY
Draw-Label "Да" 980 4215

Draw-Arrow $R13.RightX $R13.RightY $R15.LeftX $R15.LeftY
Draw-Label "Нет" 1005 4090

Draw-Line $R14.BottomX $R14.BottomY $R14.BottomX 4390
Draw-Line $R14.BottomX 4390 $R16.TopX 4390
Draw-Line $R15.BottomX $R15.BottomY $R15.BottomX 4390
Draw-Line $R15.BottomX 4390 $R16.TopX 4390
Draw-Arrow $R16.TopX 4390 $R16.TopX $R16.TopY

Draw-Arrow $R16.BottomX $R16.BottomY $R17.TopX $R17.TopY
Draw-Arrow $R17.BottomX $R17.BottomY $R18.TopX $R18.TopY
Draw-Arrow $R18.BottomX $R18.BottomY $R19.TopX $R19.TopY

Draw-Arrow $R5.LeftX $R5.LeftY $Rerr1.RightX $Rerr1.RightY
Draw-Label "Нет" 555 2755
Draw-Line $Rerr1.LeftX $Rerr1.LeftY 140 $Rerr1.LeftY
Draw-Line 140 $Rerr1.LeftY 140 $R3.CY
Draw-Arrow 140 $R3.CY $R3.LeftX $R3.LeftY

Draw-Arrow $R7.LeftX $R7.LeftY $Rerr2.RightX $Rerr2.RightY
Draw-Label "Нет" 555 3115
Draw-Line $Rerr2.LeftX $Rerr2.LeftY 120 $Rerr2.LeftY
Draw-Line 120 $Rerr2.LeftY 120 $R3.CY
Draw-Arrow 120 $R3.CY $R3.LeftX $R3.LeftY

Draw-Arrow $R9.LeftX $R9.LeftY $Rerr3.RightX $Rerr3.RightY
Draw-Label "Нет" 555 3475
Draw-Line $Rerr3.LeftX $Rerr3.LeftY 100 $Rerr3.LeftY
Draw-Line 100 $Rerr3.LeftY 100 $R3.CY
Draw-Arrow 100 $R3.CY $R3.LeftX $R3.LeftY

# Section 3
$S1 = New-Node 2300 2150 220 80
$S2 = New-Node 2260 2280 300 90
$S3 = New-Node 2260 2410 300 90
$S4 = New-Node 2260 2540 300 90
$S5 = New-Node 2260 2680 300 180
$Serr1 = New-Node 1810 2730 280 90
$S6 = New-Node 2260 2900 300 180
$Serr2 = New-Node 1810 2950 280 90
$S7 = New-Node 2260 3120 300 90
$S8 = New-Node 2260 3260 300 180
$Serr3 = New-Node 1810 3310 280 90
$S9 = New-Node 2260 3480 300 180
$Serr4 = New-Node 1810 3530 280 90
$S10 = New-Node 2260 3700 300 90
$S11 = New-Node 2260 3840 300 180
$Serr5 = New-Node 1810 3890 280 90
$S12 = New-Node 2260 4060 300 180
$Serr6 = New-Node 1810 4110 280 90
$S13 = New-Node 2260 4280 300 90
$S14 = New-Node 2260 4410 300 90
$S15 = New-Node 2260 4540 300 90
$S16 = New-Node 2260 4670 300 90
$S17 = New-Node 2260 4800 300 90
$S18 = New-Node 1840 4930 220 80

Draw-Terminator $S1 "Начало"
Draw-Input $S2 "Открытие карточки`nзадачи"
Draw-Input $S3 "Нажатие кнопки`n\"Откликнуться\""
Draw-Predefined $S4 "Поиск профиля`nволонтёра"
Draw-Decision $S5 "Профиль`nволонтёра`nнайден?"
Draw-Input $Serr1 "Профиль не`nнайден"
Draw-Decision $S6 "Волонтёр`nактивен?"
Draw-Input $Serr2 "Неактивный`nволонтёр"
Draw-Predefined $S7 "Поиск выбранной`nзадачи"
Draw-Decision $S8 "Задача`nсуществует?"
Draw-Input $Serr3 "Задача не`nнайдена"
Draw-Decision $S9 "Статус задачи`nOPEN?"
Draw-Input $Serr4 "Отклик только`nна открытую задачу"
Draw-Predefined $S10 "Проверка`nпредыдущих заявок"
Draw-Decision $S11 "Есть активная`nзаявка?"
Draw-Input $Serr5 "Активная заявка`nуже существует"
Draw-Decision $S12 "Количество`nпопыток < 2?"
Draw-Input $Serr6 "Лимит заявок`nисчерпан"
Draw-Input $S13 "Ввод комментария`nк заявке"
Draw-Process $S14 "Создание заявки`nсо статусом PENDING"
Draw-Process $S15 "Сохранение номера`nпопытки"
Draw-Process $S16 "Сохранение заявки`nв базе данных"
Draw-Input $S17 "Сообщение:`nзаявка отправлена"
Draw-Terminator $S18 "Конец"

Draw-Arrow $S1.BottomX $S1.BottomY $S2.TopX $S2.TopY
Draw-Arrow $S2.BottomX $S2.BottomY $S3.TopX $S3.TopY
Draw-Arrow $S3.BottomX $S3.BottomY $S4.TopX $S4.TopY
Draw-Arrow $S4.BottomX $S4.BottomY $S5.TopX $S5.TopY

Draw-Arrow $S5.BottomX $S5.BottomY $S6.TopX $S6.TopY
Draw-Label "Да" 2590 2870

Draw-Arrow $S6.BottomX $S6.BottomY $S7.TopX $S7.TopY
Draw-Label "Да" 2590 3090

Draw-Arrow $S7.BottomX $S7.BottomY $S8.TopX $S8.TopY
Draw-Arrow $S8.BottomX $S8.BottomY $S9.TopX $S9.TopY
Draw-Label "Да" 2590 3470

Draw-Arrow $S9.BottomX $S9.BottomY $S10.TopX $S10.TopY
Draw-Label "Да" 2590 3690

Draw-Arrow $S10.BottomX $S10.BottomY $S11.TopX $S11.TopY
Draw-Arrow $S11.BottomX $S11.BottomY $S12.TopX $S12.TopY
Draw-Label "Нет" 2590 4040

Draw-Arrow $S12.BottomX $S12.BottomY $S13.TopX $S13.TopY
Draw-Label "Да" 2590 4270

Draw-Arrow $S13.BottomX $S13.BottomY $S14.TopX $S14.TopY
Draw-Arrow $S14.BottomX $S14.BottomY $S15.TopX $S15.TopY
Draw-Arrow $S15.BottomX $S15.BottomY $S16.TopX $S16.TopY
Draw-Arrow $S16.BottomX $S16.BottomY $S17.TopX $S17.TopY

Draw-Line $S17.BottomX $S17.BottomY $S17.BottomX 4970
Draw-Line $S17.BottomX 4970 $S18.TopX 4970
Draw-Arrow $S18.TopX 4970 $S18.TopX $S18.TopY

Draw-Arrow $S5.LeftX $S5.LeftY $Serr1.RightX $Serr1.RightY
Draw-Label "Нет" 2160 2755
Draw-Arrow $S6.LeftX $S6.LeftY $Serr2.RightX $Serr2.RightY
Draw-Label "Нет" 2160 2975
Draw-Arrow $S8.LeftX $S8.LeftY $Serr3.RightX $Serr3.RightY
Draw-Label "Нет" 2160 3335
Draw-Arrow $S9.LeftX $S9.LeftY $Serr4.RightX $Serr4.RightY
Draw-Label "Нет" 2160 3555
Draw-Arrow $S11.LeftX $S11.LeftY $Serr5.RightX $Serr5.RightY
Draw-Label "Да" 2160 3915
Draw-Arrow $S12.LeftX $S12.LeftY $Serr6.RightX $Serr6.RightY
Draw-Label "Нет" 2160 4135

foreach ($err in @($Serr1, $Serr2, $Serr3, $Serr4, $Serr5, $Serr6)) {
    Draw-Line $err.BottomX $err.BottomY $err.BottomX 4970
}
Draw-Line 1950 4970 $S18.TopX 4970

$directory = Split-Path -Path $OutputPath -Parent
if ($directory -and -not (Test-Path $directory)) {
    New-Item -ItemType Directory -Path $directory | Out-Null
}

$fullOutputPath = Join-Path (Resolve-Path ".").Path $OutputPath
$bitmap.Save($fullOutputPath, [System.Drawing.Imaging.ImageFormat]::Png)

$graphics.Dispose()
$bitmap.Dispose()
$pen.Dispose()
$framePen.Dispose()
$brush.Dispose()
$whiteBrush.Dispose()
$titleFont.Dispose()
$shapeFont.Dispose()
$smallFont.Dispose()

Write-Output $OutputPath
