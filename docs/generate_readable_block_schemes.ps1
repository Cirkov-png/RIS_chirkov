param(
    [string]$OutputDir = "docs"
)

Add-Type -AssemblyName System.Drawing

$script:pen = [System.Drawing.Pen]::new([System.Drawing.Color]::Black, 4)
$script:framePen = [System.Drawing.Pen]::new([System.Drawing.Color]::Black, 2)
$script:brush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::Black)
$script:whiteBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
$script:titleFont = [System.Drawing.Font]::new("Arial", 42, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
$script:shapeFont = [System.Drawing.Font]::new("Arial", 30, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
$script:labelFont = [System.Drawing.Font]::new("Arial", 24, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)

$script:centerFormat = [System.Drawing.StringFormat]::new()
$script:centerFormat.Alignment = [System.Drawing.StringAlignment]::Center
$script:centerFormat.LineAlignment = [System.Drawing.StringAlignment]::Center

function Pt {
    param([double]$X, [double]$Y)
    [System.Drawing.PointF]::new([single]$X, [single]$Y)
}

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

function Start-Canvas {
    param(
        [int]$Width,
        [int]$Height,
        [string]$Title
    )

    $script:bitmap = [System.Drawing.Bitmap]::new($Width, $Height)
    $script:graphics = [System.Drawing.Graphics]::FromImage($script:bitmap)
    $script:graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $script:graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $script:graphics.Clear([System.Drawing.Color]::White)

    $script:graphics.DrawRectangle($script:framePen, 20, 20, $Width - 40, $Height - 40)
    $titleRect = [System.Drawing.RectangleF]::new(60, 40, $Width - 120, 70)
    $script:graphics.DrawString($Title, $script:titleFont, $script:brush, $titleRect, $script:centerFormat)
}

function Save-Canvas {
    param([string]$FileName)

    if (-not (Test-Path $OutputDir)) {
        New-Item -ItemType Directory -Path $OutputDir | Out-Null
    }

    $resolvedOutputDir = if ([System.IO.Path]::IsPathRooted($OutputDir)) {
        $OutputDir
    } else {
        Join-Path (Resolve-Path ".").Path $OutputDir
    }
    $path = Join-Path $resolvedOutputDir $FileName
    $script:bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $script:graphics.Dispose()
    $script:bitmap.Dispose()
    Write-Output $path
}

function Draw-Text {
    param(
        [string]$Text,
        [double]$X,
        [double]$Y,
        [double]$W,
        [double]$H,
        [System.Drawing.Font]$Font = $script:shapeFont
    )

    $rect = [System.Drawing.RectangleF]::new([single]$X, [single]$Y, [single]$W, [single]$H)
    $script:graphics.DrawString($Text, $Font, $script:brush, $rect, $script:centerFormat)
}

function Draw-Label {
    param(
        [string]$Text,
        [double]$X,
        [double]$Y
    )

    $rect = [System.Drawing.RectangleF]::new([single]($X - 45), [single]($Y - 18), 90, 36)
    $script:graphics.FillRectangle($script:whiteBrush, $rect)
    $script:graphics.DrawString($Text, $script:labelFont, $script:brush, $rect, $script:centerFormat)
}

function Draw-Terminator {
    param([object]$Node, [string]$Text)
    $script:graphics.DrawEllipse($script:pen, $Node.X, $Node.Y, $Node.W, $Node.H)
    Draw-Text $Text ($Node.X + 12) ($Node.Y + 8) ($Node.W - 24) ($Node.H - 16)
}

function Draw-Process {
    param([object]$Node, [string]$Text)
    $script:graphics.DrawRectangle($script:pen, $Node.X, $Node.Y, $Node.W, $Node.H)
    Draw-Text $Text ($Node.X + 12) ($Node.Y + 8) ($Node.W - 24) ($Node.H - 16)
}

function Draw-Predefined {
    param([object]$Node, [string]$Text)
    $script:graphics.DrawRectangle($script:pen, $Node.X, $Node.Y, $Node.W, $Node.H)
    $script:graphics.DrawLine($script:pen, $Node.X + 20, $Node.Y, $Node.X + 20, $Node.Y + $Node.H)
    $script:graphics.DrawLine($script:pen, $Node.X + $Node.W - 20, $Node.Y, $Node.X + $Node.W - 20, $Node.Y + $Node.H)
    Draw-Text $Text ($Node.X + 28) ($Node.Y + 8) ($Node.W - 56) ($Node.H - 16)
}

function Draw-Input {
    param([object]$Node, [string]$Text)
    $points = [System.Drawing.PointF[]]@(
        (Pt ($Node.X + 30) $Node.Y),
        (Pt ($Node.X + $Node.W) $Node.Y),
        (Pt ($Node.X + $Node.W - 30) ($Node.Y + $Node.H)),
        (Pt $Node.X ($Node.Y + $Node.H))
    )
    $script:graphics.DrawPolygon($script:pen, $points)
    Draw-Text $Text ($Node.X + 30) ($Node.Y + 8) ($Node.W - 60) ($Node.H - 16)
}

function Draw-Decision {
    param([object]$Node, [string]$Text)
    $points = [System.Drawing.PointF[]]@(
        (Pt $Node.CX $Node.Y),
        (Pt ($Node.X + $Node.W) $Node.CY),
        (Pt $Node.CX ($Node.Y + $Node.H)),
        (Pt $Node.X $Node.CY)
    )
    $script:graphics.DrawPolygon($script:pen, $points)
    Draw-Text $Text ($Node.X + 36) ($Node.Y + 32) ($Node.W - 72) ($Node.H - 64)
}

function Draw-Line {
    param([double]$X1, [double]$Y1, [double]$X2, [double]$Y2)
    $script:graphics.DrawLine($script:pen, [single]$X1, [single]$Y1, [single]$X2, [single]$Y2)
}

function Draw-ArrowHead {
    param([double]$X1, [double]$Y1, [double]$X2, [double]$Y2)
    $angle = [Math]::Atan2($Y2 - $Y1, $X2 - $X1)
    $length = 18.0
    $wing = 10.0
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
    $script:graphics.FillPolygon($script:brush, [System.Drawing.PointF[]]@($tip, $p1, $p2))
}

function Draw-Arrow {
    param([double]$X1, [double]$Y1, [double]$X2, [double]$Y2)
    Draw-Line $X1 $Y1 $X2 $Y2
    Draw-ArrowHead $X1 $Y1 $X2 $Y2
}

function Generate-ProgramScheme {
    Start-Canvas 1600 3600 "Блок-схема работы программы"

    $A = New-Node 690 120 220 90
    $B = New-Node 650 270 300 110
    $C = New-Node 650 430 300 110
    $D = New-Node 640 630 320 200
    $E = New-Node 120 930 340 110
    $F = New-Node 120 1080 340 110
    $G = New-Node 130 1230 320 200
    $H = New-Node 650 930 300 110
    $I = New-Node 640 1090 320 200
    $J = New-Node 650 1340 300 110
    $K = New-Node 610 1490 380 150
    $L = New-Node 640 1730 320 200
    $M = New-Node 650 1980 300 110
    $N = New-Node 610 2130 380 150
    $O = New-Node 1060 1980 340 110
    $P = New-Node 1020 2130 420 150
    $Q = New-Node 650 2440 300 180
    $R = New-Node 670 2720 260 100
    $S = New-Node 690 2890 220 90

    Draw-Terminator $A "Начало"
    Draw-Input $B "Запуск программы"
    Draw-Process $C "Главная страница"
    Draw-Decision $D "Пользователь`nавторизован?"
    Draw-Input $E "Форма входа /`nрегистрации"
    Draw-Predefined $F "Проверка`nданных"
    Draw-Decision $G "Вход`nуспешен?"
    Draw-Process $H "Определение`nроли"
    Draw-Decision $I "Роль =`nVOLUNTEER?"
    Draw-Process $J "Кабинет`nволонтёра"
    Draw-Process $K "Работа с разделами:`nзадачи, профиль, заявки"
    Draw-Decision $L "Роль =`nORGANIZER?"
    Draw-Process $M "Кабинет`nорганизатора"
    Draw-Process $N "Работа с разделами:`nзадачи, заявки, матчинг"
    Draw-Process $O "Панель`nкоординатора / админа"
    Draw-Process $P "Работа с разделами:`nпанель, матчинг, профили"
    Draw-Decision $Q "Выход из`nаккаунта?"
    Draw-Predefined $R "Выход из`nсистемы"
    Draw-Terminator $S "Конец"

    Draw-Arrow $A.BottomX $A.BottomY $B.TopX $B.TopY
    Draw-Arrow $B.BottomX $B.BottomY $C.TopX $C.TopY
    Draw-Arrow $C.BottomX $C.BottomY $D.TopX $D.TopY

    Draw-Arrow $D.BottomX $D.BottomY $H.TopX $H.TopY
    Draw-Label "Да" 990 880

    Draw-Arrow $D.LeftX $D.LeftY $E.RightX $E.RightY
    Draw-Label "Нет" 520 730
    Draw-Arrow $E.BottomX $E.BottomY $F.TopX $F.TopY
    Draw-Arrow $F.BottomX $F.BottomY $G.TopX $G.TopY

    Draw-Arrow $G.RightX $G.RightY $H.LeftX $H.LeftY
    Draw-Label "Да" 550 1330

    Draw-Arrow $G.LeftX $G.LeftY $E.RightX $E.RightY
    Draw-Label "Нет" 500 1330

    Draw-Arrow $H.BottomX $H.BottomY $I.TopX $I.TopY
    Draw-Arrow $I.BottomX $I.BottomY $J.TopX $J.TopY
    Draw-Label "Да" 990 1290
    Draw-Arrow $J.BottomX $J.BottomY $K.TopX $K.TopY
    Draw-Arrow $K.BottomX $K.BottomY $Q.TopX $Q.TopY

    Draw-Line $I.LeftX $I.LeftY 520 $I.LeftY
    Draw-Line 520 $I.LeftY 520 $L.CY
    Draw-Arrow 520 $L.CY $L.LeftX $L.LeftY
    Draw-Label "Нет" 560 1890

    Draw-Arrow $L.BottomX $L.BottomY $M.TopX $M.TopY
    Draw-Label "Да" 990 1930
    Draw-Arrow $M.BottomX $M.BottomY $N.TopX $N.TopY
    Draw-Arrow $N.BottomX $N.BottomY $Q.TopX $Q.TopY

    Draw-Arrow $L.RightX $L.RightY $O.LeftX $O.LeftY
    Draw-Label "Нет" 1005 1830
    Draw-Arrow $O.BottomX $O.BottomY $P.TopX $P.TopY
    Draw-Arrow $P.LeftX $P.LeftY $Q.RightX $Q.RightY

    Draw-Arrow $Q.BottomX $Q.BottomY $R.TopX $R.TopY
    Draw-Label "Да" 990 2670
    Draw-Arrow $R.BottomX $R.BottomY $S.TopX $S.TopY

    Draw-Line $Q.LeftX $Q.LeftY 520 $Q.LeftY
    Draw-Line 520 $Q.LeftY 520 $H.CY
    Draw-Arrow 520 $H.CY $H.LeftX $H.LeftY
    Draw-Label "Нет" 570 2500

    Save-Canvas "program-flow-readable.png"
}

function Generate-RegistrationScheme {
    Start-Canvas 1600 3400 "Блок-схема регистрации пользователя"

    $A = New-Node 690 120 220 90
    $B = New-Node 650 270 300 110
    $C = New-Node 650 430 300 110
    $D = New-Node 650 590 300 110
    $E = New-Node 640 750 320 200
    $Err1 = New-Node 150 800 320 100
    $F = New-Node 650 1040 300 110
    $G = New-Node 640 1200 320 200
    $Err2 = New-Node 150 1250 320 100
    $H = New-Node 650 1490 300 110
    $I = New-Node 640 1650 320 200
    $Err3 = New-Node 150 1700 320 100
    $J = New-Node 650 1940 300 110
    $K = New-Node 650 2090 300 110
    $L = New-Node 650 2240 300 110
    $M = New-Node 640 2400 320 200
    $N = New-Node 380 2670 340 110
    $O = New-Node 880 2670 320 110
    $P = New-Node 650 2850 300 110
    $Q = New-Node 650 3000 300 110
    $R = New-Node 650 3070 300 110
    $S = New-Node 690 3250 220 90

    Draw-Terminator $A "Начало"
    Draw-Input $B "Открытие формы`nрегистрации"
    Draw-Input $C "Ввод логина, email,`nпароля и роли"
    Draw-Predefined $D "Проверка`nзаполнения полей"
    Draw-Decision $E "Все поля`nкорректны?"
    Draw-Input $Err1 "Ошибка`nзаполнения"
    Draw-Predefined $F "Проверка`nлогина"
    Draw-Decision $G "Логин`nсвободен?"
    Draw-Input $Err2 "Логин уже`nзанят"
    Draw-Predefined $H "Проверка`nemail"
    Draw-Decision $I "Email`nсвободен?"
    Draw-Input $Err3 "Email уже`nзарегистрирован"
    Draw-Process $J "Определение`nроли"
    Draw-Process $K "Хеширование`nпароля"
    Draw-Process $L "Создание`nучётной записи"
    Draw-Decision $M "Роль =`nVOLUNTEER?"
    Draw-Process $N "Создание профиля`nволонтёра"
    Draw-Process $O "Без профиля`nволонтёра"
    Draw-Process $P "Генерация`nJWT-токена"
    Draw-Process $Q "Сохранение данных`nавторизации"
    Draw-Input $R "Переход на`nглавную страницу"
    Draw-Terminator $S "Конец"

    Draw-Arrow $A.BottomX $A.BottomY $B.TopX $B.TopY
    Draw-Arrow $B.BottomX $B.BottomY $C.TopX $C.TopY
    Draw-Arrow $C.BottomX $C.BottomY $D.TopX $D.TopY
    Draw-Arrow $D.BottomX $D.BottomY $E.TopX $E.TopY

    Draw-Arrow $E.BottomX $E.BottomY $F.TopX $F.TopY
    Draw-Label "Да" 990 990
    Draw-Arrow $E.LeftX $E.LeftY $Err1.RightX $Err1.RightY
    Draw-Label "Нет" 515 850
    Draw-Line $Err1.LeftX $Err1.LeftY 80 $Err1.LeftY
    Draw-Line 80 $Err1.LeftY 80 $C.CY
    Draw-Arrow 80 $C.CY $C.LeftX $C.LeftY

    Draw-Arrow $F.BottomX $F.BottomY $G.TopX $G.TopY
    Draw-Arrow $G.BottomX $G.BottomY $H.TopX $H.TopY
    Draw-Label "Да" 990 1440
    Draw-Arrow $G.LeftX $G.LeftY $Err2.RightX $Err2.RightY
    Draw-Label "Нет" 515 1300
    Draw-Line $Err2.LeftX $Err2.LeftY 90 $Err2.LeftY
    Draw-Line 90 $Err2.LeftY 90 $C.CY
    Draw-Arrow 90 $C.CY $C.LeftX $C.LeftY

    Draw-Arrow $H.BottomX $H.BottomY $I.TopX $I.TopY
    Draw-Arrow $I.BottomX $I.BottomY $J.TopX $J.TopY
    Draw-Label "Да" 990 1890
    Draw-Arrow $I.LeftX $I.LeftY $Err3.RightX $Err3.RightY
    Draw-Label "Нет" 515 1750
    Draw-Line $Err3.LeftX $Err3.LeftY 100 $Err3.LeftY
    Draw-Line 100 $Err3.LeftY 100 $C.CY
    Draw-Arrow 100 $C.CY $C.LeftX $C.LeftY

    Draw-Arrow $J.BottomX $J.BottomY $K.TopX $K.TopY
    Draw-Arrow $K.BottomX $K.BottomY $L.TopX $L.TopY
    Draw-Arrow $L.BottomX $L.BottomY $M.TopX $M.TopY

    Draw-Arrow $M.LeftX $M.LeftY $N.RightX $N.RightY
    Draw-Label "Да" 650 2485
    Draw-Arrow $M.RightX $M.RightY $O.LeftX $O.LeftY
    Draw-Label "Нет" 1290 2485

    Draw-Line $N.BottomX $N.BottomY $N.BottomX 2835
    Draw-Line $N.BottomX 2835 $P.LeftX 2835
    Draw-Arrow $P.LeftX 2835 $P.LeftX $P.LeftY

    Draw-Line $O.BottomX $O.BottomY $O.BottomX 2835
    Draw-Line $O.BottomX 2835 $P.RightX 2835
    Draw-Arrow $P.RightX 2835 $P.RightX $P.LeftY

    Draw-Arrow $P.BottomX $P.BottomY $Q.TopX $Q.TopY
    Draw-Arrow $Q.BottomX $Q.BottomY $R.TopX $R.TopY
    Draw-Arrow $R.BottomX $R.BottomY $S.TopX $S.TopY

    Save-Canvas "registration-flow-readable.png"
}

function Generate-ApplicationScheme {
    Start-Canvas 1600 3500 "Блок-схема подачи заявки на задачу"

    $A = New-Node 690 120 220 90
    $B = New-Node 650 270 300 110
    $C = New-Node 650 430 300 110
    $D = New-Node 650 590 300 110
    $E = New-Node 640 750 320 200
    $Err1 = New-Node 150 800 320 100
    $F = New-Node 640 1030 320 200
    $Err2 = New-Node 150 1080 320 100
    $G = New-Node 650 1310 300 110
    $H = New-Node 640 1470 320 200
    $Err3 = New-Node 150 1520 320 100
    $I = New-Node 640 1750 320 200
    $Err4 = New-Node 150 1800 320 100
    $J = New-Node 650 2030 300 110
    $K = New-Node 640 2190 320 200
    $Err5 = New-Node 150 2240 320 100
    $L = New-Node 640 2470 320 200
    $Err6 = New-Node 150 2520 320 100
    $M = New-Node 650 2750 300 110
    $N = New-Node 650 2900 300 110
    $O = New-Node 650 3050 300 110
    $P = New-Node 650 3200 300 110
    $Q = New-Node 690 3350 220 90

    Draw-Terminator $A "Начало"
    Draw-Input $B "Открытие карточки`nзадачи"
    Draw-Input $C "Нажатие кнопки`nОткликнуться"
    Draw-Predefined $D "Поиск профиля`nволонтёра"
    Draw-Decision $E "Профиль`nнайден?"
    Draw-Input $Err1 "Профиль не`nнайден"
    Draw-Decision $F "Волонтёр`nактивен?"
    Draw-Input $Err2 "Неактивный`nволонтёр"
    Draw-Predefined $G "Поиск`nзадачи"
    Draw-Decision $H "Задача`nсуществует?"
    Draw-Input $Err3 "Задача не`nнайдена"
    Draw-Decision $I "Статус задачи`nOPEN?"
    Draw-Input $Err4 "Отклик только`nна открытую задачу"
    Draw-Predefined $J "Проверка прошлых`nзаявок"
    Draw-Decision $K "Есть активная`nзаявка?"
    Draw-Input $Err5 "Активная заявка`nуже есть"
    Draw-Decision $L "Попыток`nменьше 2?"
    Draw-Input $Err6 "Лимит заявок`nисчерпан"
    Draw-Input $M "Ввод комментария`nк заявке"
    Draw-Process $N "Создание заявки`nPENDING"
    Draw-Process $O "Сохранение`nв базе данных"
    Draw-Input $P "Сообщение:`nзаявка отправлена"
    Draw-Terminator $Q "Конец"

    Draw-Arrow $A.BottomX $A.BottomY $B.TopX $B.TopY
    Draw-Arrow $B.BottomX $B.BottomY $C.TopX $C.TopY
    Draw-Arrow $C.BottomX $C.BottomY $D.TopX $D.TopY
    Draw-Arrow $D.BottomX $D.BottomY $E.TopX $E.TopY

    Draw-Arrow $E.BottomX $E.BottomY $F.TopX $F.TopY
    Draw-Label "Да" 990 1020
    Draw-Arrow $E.LeftX $E.LeftY $Err1.RightX $Err1.RightY
    Draw-Label "Нет" 515 850

    Draw-Arrow $F.BottomX $F.BottomY $G.TopX $G.TopY
    Draw-Label "Да" 990 1300
    Draw-Arrow $F.LeftX $F.LeftY $Err2.RightX $Err2.RightY
    Draw-Label "Нет" 515 1130

    Draw-Arrow $G.BottomX $G.BottomY $H.TopX $H.TopY
    Draw-Arrow $H.BottomX $H.BottomY $I.TopX $I.TopY
    Draw-Label "Да" 990 1740
    Draw-Arrow $H.LeftX $H.LeftY $Err3.RightX $Err3.RightY
    Draw-Label "Нет" 515 1570

    Draw-Arrow $I.BottomX $I.BottomY $J.TopX $J.TopY
    Draw-Label "Да" 990 2020
    Draw-Arrow $I.LeftX $I.LeftY $Err4.RightX $Err4.RightY
    Draw-Label "Нет" 515 1850

    Draw-Arrow $J.BottomX $J.BottomY $K.TopX $K.TopY
    Draw-Arrow $K.BottomX $K.BottomY $L.TopX $L.TopY
    Draw-Label "Нет" 990 2460
    Draw-Arrow $K.LeftX $K.LeftY $Err5.RightX $Err5.RightY
    Draw-Label "Да" 515 2290

    Draw-Arrow $L.BottomX $L.BottomY $M.TopX $M.TopY
    Draw-Label "Да" 990 2740
    Draw-Arrow $L.LeftX $L.LeftY $Err6.RightX $Err6.RightY
    Draw-Label "Нет" 515 2570

    Draw-Arrow $M.BottomX $M.BottomY $N.TopX $N.TopY
    Draw-Arrow $N.BottomX $N.BottomY $O.TopX $O.TopY
    Draw-Arrow $O.BottomX $O.BottomY $P.TopX $P.TopY
    Draw-Arrow $P.BottomX $P.BottomY $Q.TopX $Q.TopY

    foreach ($err in @($Err1, $Err2, $Err3, $Err4, $Err5, $Err6)) {
        Draw-Line $err.BottomX $err.BottomY $err.BottomX 3395
        Draw-Line $err.BottomX 3395 $Q.LeftX 3395
    }
    Draw-Arrow $Q.LeftX 3395 $Q.LeftX $Q.LeftY

    Save-Canvas "application-flow-readable.png"
}

$generated = @()
$generated += Generate-ProgramScheme
$generated += Generate-RegistrationScheme
$generated += Generate-ApplicationScheme
$generated | ForEach-Object { Write-Output $_ }

$script:pen.Dispose()
$script:framePen.Dispose()
$script:brush.Dispose()
$script:whiteBrush.Dispose()
$script:titleFont.Dispose()
$script:shapeFont.Dispose()
$script:labelFont.Dispose()
$script:centerFormat.Dispose()
