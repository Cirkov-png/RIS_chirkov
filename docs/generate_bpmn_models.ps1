param(
    [string]$OutputDir = "docs"
)

Add-Type -AssemblyName System.Drawing

$script:pen = [System.Drawing.Pen]::new([System.Drawing.Color]::Black, 4)
$script:thinPen = [System.Drawing.Pen]::new([System.Drawing.Color]::Black, 2)
$script:brush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::Black)
$script:whiteBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
$script:titleFont = [System.Drawing.Font]::new("Arial", 38, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
$script:laneFont = [System.Drawing.Font]::new("Arial", 26, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
$script:shapeFont = [System.Drawing.Font]::new("Arial", 23, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
$script:labelFont = [System.Drawing.Font]::new("Arial", 20, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
$script:smallFont = [System.Drawing.Font]::new("Arial", 19, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)

$script:centerFormat = [System.Drawing.StringFormat]::new()
$script:centerFormat.Alignment = [System.Drawing.StringAlignment]::Center
$script:centerFormat.LineAlignment = [System.Drawing.StringAlignment]::Center

function P {
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

    $titleRect = [System.Drawing.RectangleF]::new(40, 30, $Width - 80, 70)
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

    $rect = [System.Drawing.RectangleF]::new([single]($X - 40), [single]($Y - 18), 80, 36)
    $script:graphics.FillRectangle($script:whiteBrush, $rect)
    $script:graphics.DrawString($Text, $script:labelFont, $script:brush, $rect, $script:centerFormat)
}

function Get-RoundedRectPath {
    param(
        [double]$X,
        [double]$Y,
        [double]$W,
        [double]$H,
        [double]$R = 18
    )

    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $d = $R * 2
    $path.AddArc([single]$X, [single]$Y, [single]$d, [single]$d, 180, 90)
    $path.AddArc([single]($X + $W - $d), [single]$Y, [single]$d, [single]$d, 270, 90)
    $path.AddArc([single]($X + $W - $d), [single]($Y + $H - $d), [single]$d, [single]$d, 0, 90)
    $path.AddArc([single]$X, [single]($Y + $H - $d), [single]$d, [single]$d, 90, 90)
    $path.CloseFigure()
    return $path
}

function Draw-VerticalLanes {
    param(
        [double]$X,
        [double]$Y,
        [double]$W,
        [double]$H,
        [string[]]$Titles
    )

    $script:graphics.DrawRectangle($script:thinPen, [single]$X, [single]$Y, [single]$W, [single]$H)
    $headerH = 70
    $script:graphics.DrawLine($script:thinPen, [single]$X, [single]($Y + $headerH), [single]($X + $W), [single]($Y + $headerH))

    $laneW = $W / $Titles.Length
    for ($i = 1; $i -lt $Titles.Length; $i++) {
        $lineX = $X + $laneW * $i
        $script:graphics.DrawLine($script:thinPen, [single]$lineX, [single]$Y, [single]$lineX, [single]($Y + $H))
    }

    for ($i = 0; $i -lt $Titles.Length; $i++) {
        $tx = $X + $laneW * $i
        Draw-Text $Titles[$i] $tx $Y $laneW $headerH $script:laneFont
    }
}

function Draw-StartEvent {
    param([object]$Node, [string]$Text)
    $script:graphics.DrawEllipse($script:pen, $Node.X, $Node.Y, $Node.W, $Node.H)
    Draw-Text $Text ($Node.X + 10) ($Node.Y + 8) ($Node.W - 20) ($Node.H - 16) $script:smallFont
}

function Draw-EndEvent {
    param([object]$Node, [string]$Text)
    $script:graphics.DrawEllipse($script:pen, $Node.X, $Node.Y, $Node.W, $Node.H)
    $script:graphics.DrawEllipse($script:thinPen, $Node.X + 8, $Node.Y + 8, $Node.W - 16, $Node.H - 16)
    Draw-Text $Text ($Node.X + 8) ($Node.Y + 8) ($Node.W - 16) ($Node.H - 16) $script:smallFont
}

function Draw-Task {
    param([object]$Node, [string]$Text)
    $path = Get-RoundedRectPath $Node.X $Node.Y $Node.W $Node.H
    $script:graphics.DrawPath($script:pen, $path)
    Draw-Text $Text ($Node.X + 12) ($Node.Y + 8) ($Node.W - 24) ($Node.H - 16)
    $path.Dispose()
}

function Draw-Gateway {
    param([object]$Node, [string]$Text)
    $points = [System.Drawing.PointF[]]@(
        (P $Node.CX $Node.Y),
        (P ($Node.X + $Node.W) $Node.CY),
        (P $Node.CX ($Node.Y + $Node.H)),
        (P $Node.X $Node.CY)
    )
    $script:graphics.DrawPolygon($script:pen, $points)
    Draw-Text $Text ($Node.X + 28) ($Node.Y + 24) ($Node.W - 56) ($Node.H - 48)
}

function Draw-Line {
    param([double]$X1, [double]$Y1, [double]$X2, [double]$Y2)
    $script:graphics.DrawLine($script:pen, [single]$X1, [single]$Y1, [single]$X2, [single]$Y2)
}

function Draw-ArrowHead {
    param([double]$X1, [double]$Y1, [double]$X2, [double]$Y2)
    $angle = [Math]::Atan2($Y2 - $Y1, $X2 - $X1)
    $length = 16.0
    $wing = 9.0
    $p1 = P (
        $X2 - $length * [Math]::Cos($angle) + $wing * [Math]::Sin($angle)
    ) (
        $Y2 - $length * [Math]::Sin($angle) - $wing * [Math]::Cos($angle)
    )
    $p2 = P (
        $X2 - $length * [Math]::Cos($angle) - $wing * [Math]::Sin($angle)
    ) (
        $Y2 - $length * [Math]::Sin($angle) + $wing * [Math]::Cos($angle)
    )
    $tip = P $X2 $Y2
    $script:graphics.FillPolygon($script:brush, [System.Drawing.PointF[]]@($tip, $p1, $p2))
}

function Draw-PolylineArrow {
    param([System.Drawing.PointF[]]$Points)

    for ($i = 0; $i -lt $Points.Length - 1; $i++) {
        Draw-Line $Points[$i].X $Points[$i].Y $Points[$i + 1].X $Points[$i + 1].Y
    }
    Draw-ArrowHead $Points[$Points.Length - 2].X $Points[$Points.Length - 2].Y $Points[$Points.Length - 1].X $Points[$Points.Length - 1].Y
}

function Generate-MainBpmn {
    Start-Canvas 2100 3300 "1.6 BPMN-модель основного процесса предметной области"
    Draw-VerticalLanes 60 120 1980 3120 @("Организатор", "Система", "Волонтёр")

    $start = New-Node 315 220 120 120
    $createTask = New-Node 210 380 330 110
    $publishTask = New-Node 870 560 360 110
    $browseTask = New-Node 1520 740 320 110
    $applyTask = New-Node 1520 920 320 110
    $validateTask = New-Node 860 1100 380 110
    $validGateway = New-Node 950 1270 180 180

    $invalidTask = New-Node 740 1540 230 110
    $pendingTask = New-Node 1010 1540 230 110
    $invalidNotify = New-Node 1490 1540 280 110
    $invalidEnd = New-Node 1800 1535 120 120

    $reviewTask = New-Node 210 1780 330 110
    $approveGateway = New-Node 285 1940 180 180
    $rejectTask = New-Node 750 2190 250 110
    $approveTask = New-Node 1030 2190 250 110
    $rejectNotify = New-Node 1490 2190 280 110
    $rejectEnd = New-Node 1800 2185 120 120

    $performTask = New-Node 1490 2470 320 110
    $closeTask = New-Node 180 2680 390 110
    $updateTask = New-Node 840 2860 420 110
    $successEnd = New-Node 980 3040 140 140

    Draw-StartEvent $start "Старт"
    Draw-Task $createTask "Создать задачу"
    Draw-Task $publishTask "Опубликовать`nоткрытую задачу"
    Draw-Task $browseTask "Просмотреть`nдоступные задачи"
    Draw-Task $applyTask "Подать заявку"
    Draw-Task $validateTask "Проверить волонтёра,`nстатус задачи и лимит попыток"
    Draw-Gateway $validGateway "Заявка`nдопустима?"

    Draw-Task $invalidTask "Отклонить`nзапрос"
    Draw-Task $pendingTask "Создать`nPENDING"
    Draw-Task $invalidNotify "Получить уведомление`nоб ошибке"
    Draw-EndEvent $invalidEnd "Конец"

    Draw-Task $reviewTask "Рассмотреть заявку"
    Draw-Gateway $approveGateway "Одобрить`nзаявку?"
    Draw-Task $rejectTask "Установить`nREJECTED"
    Draw-Task $approveTask "Установить`nAPPROVED"
    Draw-Task $rejectNotify "Получить отказ"
    Draw-EndEvent $rejectEnd "Конец"

    Draw-Task $performTask "Выполнить задачу"
    Draw-Task $closeTask "Закрыть заявку,`nуказать результат и оценку"
    Draw-Task $updateTask "Обновить статус задачи,`nрейтинг и статистику"
    Draw-EndEvent $successEnd "Конец"

    Draw-PolylineArrow @((P $start.BottomX $start.BottomY), (P $start.BottomX $createTask.TopY), (P $createTask.TopX $createTask.TopY))
    Draw-PolylineArrow @((P $createTask.RightX $createTask.CY), (P 760 $createTask.CY), (P 760 $publishTask.CY), (P $publishTask.LeftX $publishTask.CY))
    Draw-PolylineArrow @((P $publishTask.RightX $publishTask.CY), (P 1420 $publishTask.CY), (P 1420 $browseTask.CY), (P $browseTask.LeftX $browseTask.CY))
    Draw-PolylineArrow @((P $browseTask.BottomX $browseTask.BottomY), (P $browseTask.BottomX $applyTask.TopY), (P $applyTask.TopX $applyTask.TopY))
    Draw-PolylineArrow @((P $applyTask.LeftX $applyTask.CY), (P 1380 $applyTask.CY), (P 1380 $validateTask.CY), (P $validateTask.RightX $validateTask.CY))
    Draw-PolylineArrow @((P $validateTask.BottomX $validateTask.BottomY), (P $validateTask.BottomX $validGateway.TopY), (P $validGateway.TopX $validGateway.TopY))

    Draw-PolylineArrow @((P $validGateway.LeftX $validGateway.CY), (P 700 $validGateway.CY), (P 700 $invalidTask.CY), (P $invalidTask.LeftX $invalidTask.CY))
    Draw-Label "Нет" 780 1360
    Draw-PolylineArrow @((P $validGateway.RightX $validGateway.CY), (P 1280 $validGateway.CY), (P 1280 $pendingTask.CY), (P $pendingTask.RightX $pendingTask.CY))
    Draw-Label "Да" 1210 1360

    Draw-PolylineArrow @((P $invalidTask.RightX $invalidTask.CY), (P 1410 $invalidTask.CY), (P 1410 $invalidNotify.CY), (P $invalidNotify.LeftX $invalidNotify.CY))
    Draw-PolylineArrow @((P $invalidNotify.RightX $invalidNotify.CY), (P $invalidEnd.LeftX $invalidEnd.LeftY))

    Draw-PolylineArrow @((P $pendingTask.LeftX $pendingTask.CY), (P 660 $pendingTask.CY), (P 660 $reviewTask.CY), (P $reviewTask.RightX $reviewTask.CY))
    Draw-PolylineArrow @((P $reviewTask.BottomX $reviewTask.BottomY), (P $reviewTask.BottomX $approveGateway.TopY), (P $approveGateway.TopX $approveGateway.TopY))

    Draw-PolylineArrow @((P $approveGateway.RightX $approveGateway.CY), (P 700 $approveGateway.CY), (P 700 $rejectTask.TopY), (P $rejectTask.TopX $rejectTask.TopY))
    Draw-Label "Нет" 560 2030
    Draw-PolylineArrow @((P $approveGateway.BottomX $approveGateway.BottomY), (P $approveGateway.BottomX 2145), (P $approveTask.TopX 2145), (P $approveTask.TopX $approveTask.TopY))
    Draw-Label "Да" 420 2160

    Draw-PolylineArrow @((P $rejectTask.RightX $rejectTask.CY), (P 1410 $rejectTask.CY), (P 1410 $rejectNotify.CY), (P $rejectNotify.LeftX $rejectNotify.CY))
    Draw-PolylineArrow @((P $rejectNotify.RightX $rejectNotify.CY), (P $rejectEnd.LeftX $rejectEnd.LeftY))

    Draw-PolylineArrow @((P $approveTask.RightX $approveTask.CY), (P 1450 $approveTask.CY), (P 1450 $performTask.CY), (P $performTask.LeftX $performTask.CY))
    Draw-PolylineArrow @((P $performTask.LeftX $performTask.CY), (P 1380 $performTask.CY), (P 1380 $closeTask.CY), (P $closeTask.RightX $closeTask.CY))
    Draw-PolylineArrow @((P $closeTask.RightX $closeTask.CY), (P 760 $closeTask.CY), (P 760 $updateTask.CY), (P $updateTask.LeftX $updateTask.CY))
    Draw-PolylineArrow @((P $updateTask.BottomX $updateTask.BottomY), (P $updateTask.BottomX $successEnd.TopY), (P $successEnd.TopX $successEnd.TopY))

    Save-Canvas "bpmn-main-process.png"
}

function Generate-RegistrationBpmn {
    Start-Canvas 1800 3100 "1.6 BPMN-модель процесса регистрации и доступа"
    Draw-VerticalLanes 60 120 1680 2860 @("Пользователь", "Система")

    $start = New-Node 370 220 120 120
    $openForm = New-Node 240 390 380 110
    $enterData = New-Node 240 570 380 110
    $validate = New-Node 1080 700 380 110
    $validGateway = New-Node 1180 870 180 180
    $showError = New-Node 980 1080 300 110
    $createUser = New-Node 1080 1270 380 110
    $roleGateway = New-Node 1180 1450 180 180
    $createVolunteer = New-Node 1080 1710 380 110
    $mergeGateway = New-Node 1180 1890 180 180
    $generateToken = New-Node 1080 2140 380 110
    $saveAuth = New-Node 1080 2320 380 110
    $enterSystem = New-Node 240 2520 380 110
    $end = New-Node 370 2710 120 120

    Draw-StartEvent $start "Старт"
    Draw-Task $openForm "Открыть форму`nрегистрации"
    Draw-Task $enterData "Ввести логин, email,`nпароль и роль"
    Draw-Task $validate "Проверить заполнение,`nлогин и email"
    Draw-Gateway $validGateway "Данные`nкорректны?"
    Draw-Task $showError "Показать`nошибку"
    Draw-Task $createUser "Создать учётную`nзапись"
    Draw-Gateway $roleGateway "Роль =`nVOLUNTEER?"
    Draw-Task $createVolunteer "Создать профиль`nволонтёра"
    Draw-Gateway $mergeGateway "Слияние"
    Draw-Task $generateToken "Сгенерировать токен`nи сессию"
    Draw-Task $saveAuth "Сохранить данные`nавторизации"
    Draw-Task $enterSystem "Получить доступ`nк системе"
    Draw-EndEvent $end "Конец"

    Draw-PolylineArrow @((P $start.BottomX $start.BottomY), (P $start.BottomX $openForm.TopY), (P $openForm.TopX $openForm.TopY))
    Draw-PolylineArrow @((P $openForm.BottomX $openForm.BottomY), (P $openForm.BottomX $enterData.TopY), (P $enterData.TopX $enterData.TopY))
    Draw-PolylineArrow @((P $enterData.RightX $enterData.CY), (P 930 $enterData.CY), (P 930 $validate.CY), (P $validate.LeftX $validate.CY))
    Draw-PolylineArrow @((P $validate.BottomX $validate.BottomY), (P $validate.BottomX $validGateway.TopY), (P $validGateway.TopX $validGateway.TopY))

    Draw-PolylineArrow @((P $validGateway.LeftX $validGateway.CY), (P 940 $validGateway.CY), (P 940 $showError.CY), (P $showError.LeftX $showError.CY))
    Draw-Label "Нет" 1020 965
    Draw-PolylineArrow @((P $showError.LeftX $showError.CY), (P 760 $showError.CY), (P 760 $enterData.CY), (P $enterData.RightX $enterData.CY))

    Draw-PolylineArrow @((P $validGateway.BottomX $validGateway.BottomY), (P $validGateway.BottomX $createUser.TopY), (P $createUser.TopX $createUser.TopY))
    Draw-Label "Да" 1390 1180
    Draw-PolylineArrow @((P $createUser.BottomX $createUser.BottomY), (P $createUser.BottomX $roleGateway.TopY), (P $roleGateway.TopX $roleGateway.TopY))

    Draw-PolylineArrow @((P $roleGateway.BottomX $roleGateway.BottomY), (P $roleGateway.BottomX $createVolunteer.TopY), (P $createVolunteer.TopX $createVolunteer.TopY))
    Draw-Label "Да" 1410 1675

    Draw-PolylineArrow @((P $roleGateway.RightX $roleGateway.CY), (P 1510 $roleGateway.CY), (P 1510 $mergeGateway.CY), (P $mergeGateway.RightX $mergeGateway.RightY))
    Draw-Label "Нет" 1520 1700
    Draw-PolylineArrow @((P $createVolunteer.BottomX $createVolunteer.BottomY), (P $createVolunteer.BottomX $mergeGateway.TopY), (P $mergeGateway.TopX $mergeGateway.TopY))

    Draw-PolylineArrow @((P $mergeGateway.BottomX $mergeGateway.BottomY), (P $mergeGateway.BottomX $generateToken.TopY), (P $generateToken.TopX $generateToken.TopY))
    Draw-PolylineArrow @((P $generateToken.BottomX $generateToken.BottomY), (P $generateToken.BottomX $saveAuth.TopY), (P $saveAuth.TopX $saveAuth.TopY))
    Draw-PolylineArrow @((P $saveAuth.LeftX $saveAuth.CY), (P 900 $saveAuth.CY), (P 900 $enterSystem.CY), (P $enterSystem.RightX $enterSystem.CY))
    Draw-PolylineArrow @((P $enterSystem.BottomX $enterSystem.BottomY), (P $enterSystem.BottomX $end.TopY), (P $end.TopX $end.TopY))

    Save-Canvas "bpmn-registration-process.png"
}

$generated = @()
$generated += Generate-MainBpmn
$generated += Generate-RegistrationBpmn
$generated | ForEach-Object { Write-Output $_ }

$script:pen.Dispose()
$script:thinPen.Dispose()
$script:brush.Dispose()
$script:whiteBrush.Dispose()
$script:titleFont.Dispose()
$script:laneFont.Dispose()
$script:shapeFont.Dispose()
$script:labelFont.Dispose()
$script:smallFont.Dispose()
$script:centerFormat.Dispose()
