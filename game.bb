If CommandLine$() <> "dev"
	Print "W S A D = Movem personagem"
	Print "E = Aperta bot�es"
	Print "Espa�o = Voa"
	Print "Mouse Direito = Equipa arma"
	Print "Mouse Esquerdo = Atira"
	Print ""
	Print "Precione qualquer merda pra continua!"
	WaitKey
EndIf

;SoundRoot$ = "Sons/"
ModelRoot$ = "Modelos/"

AppTitle "Pexe's Worldcraft"
Graphics3D 800,600,16,2
SetBuffer BackBuffer()

Include "worldcraft.bb"

;Seta Variaveis
Type Motion
	Field Mesh%    ;Mesh para mover
	Field ix#,iy#,iz# ;Incremento
	Field mx#,my#,mz# ;Maximo
	Field dx#,dy#,dz# ;Ja movido
	Field ixr#,iyr#,izr# ;Incremento ROTATION
	Field mxr#,myr#,mzr# ;Maximo ROTATION
	Field dxr#,dyr#,dzr# ;Ja movido ROTATION
	Field SoundStop$,SoundStop2%
	Field DelayTm%, DelaySt%
	Field O_Wait%,O_WaitNum
End Type

Type Message
	Field x#,y#,cx%,cy%
	Field texto$
	Field colorr, colorg, colorb
	Field time%
End Type

Global SOUND_COUNT%
Type Sound
	Field Sound%,Num%
	Field Channel%
	Field Played%
	Field Loop%=1
	Field DelayTm, DelaySt
	Field mesh%
End Type

Type Bullet
	Field x#,y#,z#
	Field p#,w#,r#
	Field speed#
	Field decal$
	Field decal_load%
	Field hit%
	Field hand%
End Type

Global Player
Global Camera
Global SoundRoot$
Global Gun_InHand = 0

;DEBUG
Global Vis% = 1000

;Variaveis de Colis�o
Global Colision_Player     = 1
Global Colision_Worldspawn = 2
Global Colision_Bullet     = 3
Global Colision_Triggers   = 4

PXWC_LoadMap("Mapas/mapa_teste.map")
PXWC_BuildMap()

;Acha a entidade Player Start
Player = Find_EntityByClassname%("player_start")
If Player = -1 Then RuntimeError("Player_Start n�o definido")

Origem$ = GetProperty(Player,"origin")
px = Explode(Origem$,0," ")
pz = Explode(Origem$,LAST_EXPLODE_CHAR%+1," ")
py = Substr(Origem$,LAST_EXPLODE_CHAR%+1,Len(Origem$)-(LAST_EXPLODE_CHAR%))

;Cria Camera e Pivot
Player = CreatePivot()
Player_Mesh = CreateSphere(16,Player)
Camera = CreateCamera(Player)
Listener = CreateListener(Camera,1/MAP_SCALE)
PositionEntity Player,px/MAP_SCALE,py/MAP_SCALE,pz/MAP_SCALE
MoveEntity Camera,0,30/MAP_SCALE,0
CameraRange Camera,0.1,100
HidePointer:MoveMouse GraphicsWidth()/2,GraphicsHeight()/2
MouseXSpeed():MouseYSpeed()
ScaleEntity Player_Mesh,13/MAP_SCALE,44/MAP_SCALE,13/MAP_SCALE

;Carrega Modelos
	;Gun1
	Global Gun1_Model% = LoadMesh("Modelos/gun.b3d", Camera)
	ScaleMesh Gun1_Model%,1/MAP_SCALE,1/MAP_SCALE,1/MAP_SCALE
	PositionEntity Gun1_Model%,EntityX(Camera,1)+(31/MAP_SCALE),EntityY(Camera,1)-(9/MAP_SCALE),EntityZ(camera,1)+(6/MAP_SCALE),1
	RotateEntity Gun1_Model%,EntityPitch(Camera,1), EntityYaw(Camera,1)+95,EntityRoll(camera,1),0
	For a = 1 To 50 ;Esconde arma
		PositionMesh Gun1_Model,0,-.005,0
		RotateMesh Gun1_Model,1,0,-.5
	Next

;Carrega Sprites
Global Crosshair% = LoadImage("Sprites/cross.bmp")

;Seta as colis�es por tipo
EntityType Player, Colision_Player
EntityRadius Player,23/MAP_SCALE,44/MAP_SCALE

For s.surface = Each surface
	If s\texture$ = "ORIGIN"
		m.mesh = Find_Mesh(s\Mesh_Num)
		HideEntity (m\hand)
		If GetProperty(m\entity,"classname") = "door_rotating"
			If GetProperty(m\entity,"pivot") = ""
				pivot = CreatePivot()
				SetProperty(m\entity,"pivot",pivot)
				PositionEntity pivot,EntityX(m\hand),EntityY(m\hand),EntityZ(m\hand)
			EndIf
		EndIf
	ElseIf s\texture$ = "CLIP"
		m.mesh = Find_Mesh(s\Mesh_Num)
		EntityAlpha m\hand,0
	EndIf
Next

For m.mesh = Each mesh
	EntityType m\hand, Colision_Worldspawn 
	If GetProperty(m\entity,"classname") = "door_rotating"
		pivot = GetProperty(m\entity,"pivot")
		EntityParent m\hand,pivot
	EndIf
Next

;Bota o alpha, acha o SoundRoot$ e Esconde Triggers
For p.property = Each property
	;Esconde triggers
	If (p\name = "classname" And p\value$ = "trigger_multiple") Or (p\name = "classname" And p\value$ = "trigger_once")
		For a = 0 To Count_MeshByEntity(p\entity_num)-1
			ent.mesh = Find_MeshByEntity.Mesh (p\entity_num,a)
			EntityAlpha ent\hand,0
			EntityType ent\hand,Colision_Triggers
		Next
	EndIf
	
	;Bota alpha
	If p\name$ = "alpha"
		count = Count_MeshByEntity(p\entity_num)
		For a = 0 To Count-1
			m.mesh = Find_MeshByEntity(p\entity_num,a)
			EntityAlpha (m\hand,Float(p\value$)/100)
		Next
	EndIf
	
	;Pega SoundRoot$
	If p\name$ = "classname" And p\value$ = "worldspawn"
		SoundRoot$ = GetProperty(p\entity_num,"sound")
	EndIf
	
	;Esconder hiddens
	If p\name$ = "classname" And p\value$ = "hidden"
		For a = 0 To Count_MeshByEntity(p\entity_num)-1
			ent.mesh = Find_MeshByEntity.Mesh (p\entity_num,a)
			HideEntity ent\hand
		Next
	EndIf
	
	If p\name$ = "classname" And p\value$ = "light"
		Origem$ = GetProperty(p\entity_num,"origin")
		px = Explode(Origem$,0," ")
		pz = Explode(Origem$,LAST_EXPLODE_CHAR%+1," ")
		py = Substr(Origem$,LAST_EXPLODE_CHAR%+1,Len(Origem$)-(LAST_EXPLODE_CHAR%))
		luz = CreateLight(2)
		PositionEntity luz,px/MAP_SCALE,py/MAP_SCALE,pz/MAP_SCALE
	EndIf
Next

;Cria Colis�es
Collisions Colision_Player, Colision_Worldspawn,2,3
Collisions Colision_Player, Colision_Triggers,2,3
Collisions Colision_Bullet, Colision_Worldspawn,2,3

SetBuffer BackBuffer()
Gun1_AnimC1 = -1
Gun1_AnimC2 = 0

;MAP_AUTO
For e.entity = Each entity
	If GetProperty$(e\num, "classname") = "map_auto"
		For p.property = Each property
			If p\entity_num = e\num
				If Left(p\name$,6) = "action" ;Inicia uma a��o
					If Len(p\name$) > 6 Then Lest$ = Right(p\name$,Len(p\name)-6)
					Execute(p\value$)
				EndIf
			EndIf
		Next
	EndIf
Next


While Not KeyHit(1)
	If KeyDown(57) Then TranslateEntity Player,0,10/MAP_SCALE,0

	If KeyDown(74) Then Vis = Vis - 1	
	If KeyDown(78) Then Vis = Vis + 1
	CameraRange camera, 1,Vis%
	
	Moved = 0	
	;Teclas
	If KeyDown(32) Then MoveEntity Player,4/MAP_SCALE,0,0:Moved=2 ;direita
	If KeyDown(30) Then MoveEntity Player,-4/MAP_SCALE,0,0:Moved=2 ;esquerda
	If KeyDown(17) Then MoveEntity Player,0,0,4/MAP_SCALE:Moved=1 ;cima
	If KeyDown(31) Then MoveEntity Player,0,0,-4/MAP_SCALE:Moved=1 ;baixo
	If MouseHit(2) Then If Gun_InHand = 0 Then Gun_InHand = 1:ElseIf Gun_InHand = 50 Then Gun_InHand = -50
	If MouseHit(1) Then If (Gun_InHand = 50) And (Shot = 0) Then Shot = 1
		
	If KeyHit(18) ;E
		For e.entity = Each entity
			If GetProperty$(e\num, "classname") = "button"
				NM% = Count_MeshByEntity (e\num%)
				If NM = 0 Then RuntimeError("erro")
				For a = 0 To NM-1
					m1.mesh = Find_MeshByEntity.Mesh (e\num,a)
					If EntityDistance(Camera,m1\hand) < 45/MAP_SCALE Then
						For p.property = Each property
							If p\entity_num = e\num
								If Left(p\name$,6) = "action" ;Inicia uma a��o
									If Len(p\name$) > 6 Then Lest$ = Right(p\name$,Len(p\name)-6)
									Ex = GetProperty(e\num,"executed"+Lest$)
									Rp = GetProperty(e\num,"repeat"+Lest$)
									If Rp = 0 Or Ex < Rp
										SetProperty(e\num,"executed"+Lest$,Ex+1)
										Execute(p\value$)
									EndIf
								EndIf
							EndIf
						Next
					EndIf
				Next
			EndIf
		Next
	EndIf
		
	;Trigger
	For p.property = Each property
		If (p\name$ = "classname" And p\value$ = "trigger_multiple") Or (p\name$ = "classname" And p\value$ = "trigger_once")
			NM% = Count_MeshByEntity (p\entity_num)
			For a = 0 To NM-1
				m.mesh = Find_MeshByEntity.Mesh (p\entity_num,a)
				If EntityCollided (m\hand, Colision_Player)
					If Int(GetProperty(p\entity_num,"on_execution")) = "0"
						For p2.property = Each property
							If p2\entity_num = p\entity_num
								If Left(p2\name$,6) = "action" ;Inicia uma a��o
									If Len(p2\name$) > 6 Then Lest$ = Right(p2\name$,Len(p2\name)-6)
									Ex = Int(GetProperty(p\entity_num,"executed"+Lest$))
									Rp = Int(GetProperty(p\entity_num,"repeat"+Lest$))
									If Rp = 0 Or Ex < Rp
										SetProperty(p\entity_num,"executed"+Lest$,Ex+1)
										Execute(p2\value$)
									EndIf
								EndIf
							EndIf
						Next
						NM2% = Count_MeshByEntity (p\entity_num)
						For c = 0 To NM2-1
							m2.mesh = Find_MeshByEntity.Mesh (p\entity_num,c)
							HideEntity m2\hand
							SetProperty(p\entity_num,"on_execution",MilliSecs())
						Next
						Goto Sai_Trigger_Multiple
					EndIf
				EndIf
			Next
			If GetProperty(p\entity_num,"on_execution") > 0 And (p\name$ = "classname" And p\value$ = "trigger_multiple")
				If (Int(GetProperty(p\entity_num,"on_execution")) + (Int(GetProperty(p\entity_num,"reset"))*1000)) < MilliSecs() Then
					SetProperty(p\entity_num,"on_execution",0)
					For c = 0 To NM-1
						m2.mesh = Find_MeshByEntity.Mesh (p\entity_num,c)						
						ShowEntity m2\hand
					Next
					
				EndIf
			EndIf
		EndIf
		.Sai_Trigger_Multiple
	Next
	If Vis_Changed% = 0 Then CameraRange camera,1,1000
	Vis_Changed% = 0

	;Atira
	If Shot = 1
		Shot = 2
		AddSound(SoundRoot$+"shoot.mp3")
		b.bullet = New bullet
		b\x# = EntityX(Player)
		b\y# = EntityY(Player)+30/MAP_SCALE
		b\z# = EntityZ(PLayer)
		b\p# = EntityPitch(Camera)
		b\w# = EntityYaw(PLayer)
		b\speed = 5
		b\decal$ = "Decals/{BIGSHOT1.png"
		
		b\hand = CreatePivot()
		PositionEntity b\hand, b\x, b\y, b\z, 1
		RotateEntity b\hand, b\p, b\w, 0	
	
		EntityType (b\hand,Colision_Bullet)
		EntityRadius b\hand,.1
	EndIf

	If Shot > 1 And Shot < 5
		PositionMesh Gun1_Model,-.2,0,0
		RotateMesh Gun1_Model,0,0,6
		Shot = Shot + 1
	EndIf
	
	If Shot >= 5 And Shot < 15
		PositionMesh Gun1_Model,.06,.0041,0
		RotateMesh Gun1_Model,0,0,-1.8
		Shot = Shot + 1
	EndIf
	If Shot = 15 Then Shot = 0
	
	For b.bullet = Each bullet
		If b\hit = 0
			If EntityCollided(b\hand, Colision_Worldspawn)
				b\hit% = 1
				b\decal_load = LoadImage(b\decal$)
				MaskImage b\decal_load,255,255,255
				For m.mesh = Each mesh
					If EntityCollided(m\hand, Colision_Bullet)
						col_x# = CollisionX(m\hand,1)*MAP_SCALE#
						col_y# = CollisionY(m\hand,1)*MAP_SCALE#
						col_z# = CollisionZ(m\hand,1)*MAP_SCALE#
						hand% = CollisionSurface(m\hand,1)
						s.surface = Find_SurfaceByHandle(hand%)
						t.triangle = Find_TriangleBySurface(s\num)
										
						If t\v1 = t\v2 Then TU# = (t\u1-t\u2)
						If t\v1 = t\v3 Then TU# = (t\u1-t\u3)
						If t\v3 = t\v2 Then TU# = (t\u3-t\u2)
				
						If t\u1 = t\u2 Then TV# = (t\v1-t\v2)
						If t\u1 = t\u3 Then TV# = (t\v1-t\v3)
						If t\u3 = t\u2 Then TV# = (t\v3-t\v2)
												
						VU# = ((col_x# * t\tv1x + col_y# * t\tv1z + col_z# * t\tv1y)/s\scalex + s\shiftx)/s\IW
						;VU = VU + TU
						
						VV# = ((col_x# * t\tv2x + col_y# * t\tv2z + col_z# * t\tv2y)/s\scaley + s\shifty)/s\IH
						;VV = VV + TV
											
						DebugLog t\x1+" / "+t\y2+" / "+t\z3
						DebugLog col_x+" / "+col_y+" / "+col_z
						DebugLog "--------------------------------"
						DebugLog "Surface Number: "+s\num
						DebugLog "Surface Texture: "+s\texture
						DebugLog "Triangle U: "+t\u1+" / "+t\u2+" / "+t\u3
						DebugLog "Triangle V: "+t\v1+" / "+t\v2+" / "+t\v3
						DebugLog "Size UV: "+TU+" / "+TV
						DebugLog "Texture Size: "+tx+" / "+ty
						DebugLog "VU/VV: "+VU+" / "+VV
						;RuntimeError("DEBUG")
						
						x.texture = Find_TextureBySurface(s\num)
						SetBuffer (TextureBuffer(x\decal_layer))
						DrawImage b\decal_load,40,40;Abs(VU),Abs(VV)
						SetBuffer BackBuffer()
					EndIf
				Next
				bb = CreateCube()
				ScaleEntity bb,.1,.1,.1
				PositionEntity bb, EntityX(b\hand),EntityY(b\hand),EntityZ(b\hand)
			Else
				MoveEntity b\hand,0,0,b\speed
			EndIf
		EndIf
	Next
	
	;Free-Look
	mcx# = Float(MouseXSpeed()) * 0.1
	mcy# = Float(MouseYSpeed()) * 0.1
	MoveMouse GraphicsWidth()/2,GraphicsHeight()/2
	RotateEntity Player,0,EntityYaw(Player)-mcx#,0,1
	RotateEntity Camera,EntityPitch(camera)+mcy#,0,0
	
	;Anima��o Arma
	If Gun_InHand = 0 Then HideEntity Gun1_Model:Else:ShowEntity Gun1_Model
	
	If Gun_InHand < 0 Then
			PositionMesh Gun1_Model,0,-.005,0
			RotateMesh Gun1_Model,1,0,-.5
			Gun_InHand = Gun_InHand + 1
	EndIf
	
	If Gun_InHand > 0 And Gun_InHand < 50 Then
			PositionMesh Gun1_Model,0,.005,0
			RotateMesh Gun1_Model,-1,0,.5
			Gun_InHand = Gun_InHand + 1
	EndIf
	
	Gun1_Anim1 = Gun1_Anim1 + 1
	If Gun1_Anim1 > 150 Then
		Gun1_Anim1 = 0
		If Gun1_AnimC1 = 1 Then Gun1_AnimC1 = -1:Else:Gun1_AnimC1 = 1
	EndIf
	
	If Moved = 1
		If Gun1_AnimC2 = 0 Then Gun1_AnimC2 = 1
		Gun1_Anim2 = Gun1_Anim2 + 1
		If Gun1_Anim2 > 15 Then
			If Gun1_AnimC2 = 1 Then Gun1_AnimC2 = -1:Else:Gun1_AnimC2 = 1
			Gun1_Anim2 = 0
		EndIf
	Else
		If Gun1_AnimC2 = -1 Then Gun1_Anim2=15-Gun1_Anim2
		If Gun1_Anim2 > 0
			For a=0 To Gun1_Anim2 Step 1
				PositionMesh Gun1_Model%,(Float(-1)/10)/MAP_SCALE,0,0
			Next
		EndIf
		Gun1_Anim2 = 0
		Gun1_AnimC2 = 0	
	EndIf

	PositionMesh Gun1_Model,(Float(Gun1_AnimC2)/10)/MAP_SCALE,0,(Float(Gun1_AnimC1)*(Abs(Gun1_Anim1*2)-150)/3500)/MAP_SCALE
	RotateMesh Gun1_Model,0,-(Float(Gun1_AnimC1)/50),0
	
	;Gravidade
	TranslateEntity Player,0,-6/MAP_SCALE,0
	
	;Motion
	For o.motion = Each motion
		If o\DelayTm+o\DelaySt < MilliSecs()
			If (Is_Moving(o\O_Wait%) <= o\O_WaitNum%) Or (o\O_Wait% = 0)
				If o\ix  <> 0 Or o\iy  <> 0 Or o\iz  <> 0 Then PositionMesh o\mesh,o\ix/MAP_SCALE,o\iy/MAP_SCALE,o\iz/MAP_SCALE
				If o\ixr <> 0 Or o\iyr <> 0 Or o\izr <> 0 Then TurnEntity   o\mesh,o\ixr,o\iyr,o\izr
				o\dx = o\dx + Abs(o\ix)
				o\dy = o\dy + Abs(o\iy)
				o\dz = o\dz + Abs(o\iz)
				o\dxr = o\dxr + Abs(o\ixr)
				o\dyr = o\dyr + Abs(o\iyr)
				o\dzr = o\dzr + Abs(o\izr)
				If (o\dx >= o\mx*MAP_SCALE) And (o\dy >= o\my*MAP_SCALE) And (o\dz >= o\mz*MAP_SCALE) Then 
					If (o\dxr >= o\mxr) And (o\dyr >= o\myr) And (o\dzr >= o\mzr) Then
						If Len(o\SoundStop) > 0 Then AddSound(o\SoundStop$,1,0,0,o\mesh):StopSound(o\SoundStop2) ;Adciona Sons
						Delete o.motion
					EndIf
				EndIf
			Else
				o\DelaySt = MilliSecs()
			EndIf
		EndIf
	Next
	
	;Sound
	For d.Sound = Each Sound
		If d\Sound = 0 Then Delete d:Goto Skip_Sound
		If d\DelayTm+d\DelaySt < MilliSecs()
			If Not ChannelPlaying(d\Channel)
				If d\Played = d\Loop And d\Loop > 0 Then FreeSound d\Sound:Delete d:Goto Skip_Sound
				d\Played = d\Played + 1
				If d\mesh = 0
					d\Channel = PlaySound (d\Sound)
				Else
					d\Channel = EmitSound (d\Sound,d\mesh%)
				EndIf
			EndIf
		EndIf
		.Skip_Sound
	Next
	
	If KeyDown(2) WireFrame 1:Else:WireFrame 0
	
	;RENDERIZA
	UpdateWorld
	RenderWorld
	
	;Text
	For e.entity = Each entity
		If GetProperty$(e\num, "classname") = "subtitle" And Int(GetProperty$(e\num, "on_execution")) > 0
			If Int(GetProperty$(e\num, "on_execution")) + 3000 < MilliSecs() Then Execute(GetProperty$(e\num, "name"),1)
		EndIf
	Next
	For g.message = Each message
		If g\time% < MilliSecs() Then
			Delete g
		Else
			color_r = ColorRed():color_g = ColorGreen():color_b = ColorBlue()
			Color g\colorr,g\colorg,g\colorb
			Text g\x,g\y,g\texto$,g\cx,g\cy
			Color color_r,color_g,color_b
		EndIf
	Next
	
	If Gun_InHand > 0 Then DrawImage Crosshair%,(GraphicsWidth()/2)-(ImageWidth(Crosshair%)/2),(GraphicsHeight()/2)-(ImageHeight(Crosshair%)/2)
	Text 0,0,Vis
	Text 0,15,TrisRendered()
	Text 0,30,Gun1_AnimC2
	Text 0,45,Gun1_Anim2
	Flip
Wend

Function Execute(E_name$, Internal%=0)
	For e.entity = Each entity
		If E_name$ = GetProperty$(e\num, "name")
			
			Select GetProperty$(e\num, "classname")
			Case "door"		
				Som$ = GetProperty$(e\num, "sound")
				Som2$ = GetProperty$(e\num, "sound_stop")
				Origem$ = GetProperty(e\num,"angles")
				ap = Explode(Origem$,0," ")
				ay = Explode(Origem$,LAST_EXPLODE_CHAR%+1," ")
				D_Delay = GetProperty(e\num,"delay")
				C_Delay = GetProperty(e\num,"close_delay")
				
				Open = Int(GetProperty(e\num,"open"))
				If C_Delay < 0 Then SetProperty(e\num, "open",Abs(open-1))
				If Open = 1 Then Open = -1:Else:Open = 1
				
				NM = Count_MeshByEntity (e\num%)
				VMW% = 0:VMH% = 0:VMD% = 0
				For a=0 To NM-1
					m.mesh = Find_MeshByEntity.Mesh (e\num,a)
					If MeshWidth(m\hand)  > VMW Then VMW = MeshWidth(m\hand)
					If MeshHeight(m\hand) > VMH Then VMH = MeshHeight(m\hand)
					If MeshDepth(m\hand) > VMD Then VMD = MeshDepth(m\hand)
				Next
				IncrementX = 0:IncrementY = 0:IncrementZ = 0
				MaxX = 0:MaxY = 0:MaxZ = 0
				
				If ay <> 0
					IncrementX = Cos(ay)
					IncrementZ = Sin(ay)
					MaxX = Abs(Cos(ay)*VMW)
					MaxZ = Abs(Sin(ay)*VMD)
				EndIf
					
				If ap <> 0
					IncrementY = 1
					MaxY = VMH
				EndIf
				
				Cancel_move = 0
				NM = Count_MeshByEntity (e\num%)
				For a=0 To NM-1
					If Is_Moving%(m\hand%) = 0
						o.motion = New motion
						o\DelayTm = Int(D_Delay)*1000
						o\DelaySt = MilliSecs()
						o\mesh = m\hand
						
						o\ix = IncrementX*Open
						o\iy = IncrementY*Open
						o\iz = IncrementZ*Open
						o\mx = MaxX
						o\my = MaxY
						o\mz = MaxZ
						
						If a = NM-1 Then 
							If Len(Som2$) > 0 Then o\SoundStop$ = SoundRoot$+"/"+Som2$:SoundStop2% = Som3%
							If Len(Som$) > 0 Then Som3% = AddSound(SoundRoot$+"/"+Som$,1,Int(D_Delay)*1000,0,o\mesh)
						EndIf
					Else
						Cancel_move = 1
					EndIf
				Next	
				
				If C_Delay >= 0
					NM = Count_MeshByEntity (e\num%)
					For a=0 To NM-1
						If Cancel_move = 0
							o.motion = New motion
							o\DelayTm = Int(C_Delay)*1000
							o\DelaySt = MilliSecs()
							o\mesh = m\hand
							o\O_Wait% = m\hand
							o\O_WaitNum = 1
						
							o\ix = IncrementX*Open*-1
							o\iy = IncrementY*Open*-1
							o\iz = IncrementZ*Open*-1
							o\mx = MaxX
							o\my = MaxY
							o\mz = MaxZ
						
							If a = NM-1 Then 
								If Len(Som2$) > 0 Then o\SoundStop$ = SoundRoot$+"/"+Som2$:SoundStop2% = Som3%
								If Len(Som$) > 0 Then Som3% = AddSound(SoundRoot$+"/"+Som$,1,Int(D_Delay)*1000,Int(C_Delay)*1000,o\mesh)
							EndIf
						EndIf
					Next
				EndIf
			Case "door_rotating"
				pivot = GetProperty(e\num,"pivot")
				If Is_Moving%(pivot) = 0
					o.motion = New motion
					
					Som$ = GetProperty$(e\num, "sound")
					Som2$ = GetProperty$(e\num, "sound_stop")
					If Len(Som$) > 0 Then Som3% = AddSound(SoundRoot$+"/"+Som$,1,Int(GetProperty(e\num,"delay"))*1000,0,pivot)
					If Len(Som2$) > 0 Then o\SoundStop$ = SoundRoot$+"/"+Som2$:SoundStop2% = Som3%
					
					o\mesh = pivot
					o\DelayTm = Int(GetProperty(e\num,"delay"))*1000
					o\DelaySt = MilliSecs()
					Open = Int(GetProperty(e\num,"open"))
					SetProperty(e\num, "open",Abs(open-1))
					Invert = 1
					If Open = 0 Then Invert = 1:Else:Invert = -1
				
					o\ixr = Float(Invert)*Int(GetProperty(e\num,"rot_speed_pitch"))
					o\iyr = Float(Invert)*Int(GetProperty(e\num,"rot_speed_yaw"))
					o\izr = Float(Invert)*Int(GetProperty(e\num,"rot_speed_roll"))
					o\mxr = Int(GetProperty(e\num,"open_degrees_pitch"))
					o\myr = Int(GetProperty(e\num,"open_degrees_yaw"))
					o\mzr = Int(GetProperty(e\num,"open_degrees_roll"))
				EndIf
			Case "subtitle"
				HasExecuted% = 0
				For p.property = Each property
					If p\entity_num = e\num
						If Left(p\name$,4) = "text" ;Inicia um texto
							If Len(p\name$) > 4 Then Lest$ = Right(p\name$,Len(p\name)-4)
							Ex = Int(GetProperty(p\entity_num,"executed"))
							Fx = Int(GetProperty(p\entity_num,"on_execution"))
							Person$ = GetProperty(p\entity_num,"person"+Lest$)
							Lest2% = Int(Right(Lest$,Len(Lest$)-1))
							If (((Len(Lest$) = 0) And (Fx = 0)) Or ((Len(Lest$) > 0) And ((Lest2%-1 = Ex) Or (Fx = 0 And Lest2% = 1))) And Internal% = 1) Or (Internal% = 0 And (Fx = 0 And Lest2% = 1))
								SetProperty(p\entity_num,"executed",Ex+1)
								SetProperty(p\entity_num,"on_execution",MilliSecs())
								HasExecuted% = 1
								g.message = New message
								g\x = 400
								g\y = 450
								g\cx = 1
								g\cy = 1
								g\texto$ = Person$ + ": " +GetProperty(p\entity_num,"text"+Lest$)
								g\colorr = 255
								g\colorg = 0
								g\colorb = 0
								g\time = MilliSecs()+3000
								Goto Skip_Subtitle
							EndIf
						EndIf
					EndIf
				Next
				.Skip_Subtitle
				If HasExecuted% = 0 And Internal% = 1 Then ;Reseta entidade

					SetProperty(e\num,"executed",0)
					SetProperty(e\num,"on_execution",0)
				EndIf
			End Select
		EndIf
	Next
End Function

Function Is_Moving%(mesh%)
	cc = 0
	For o.motion = Each motion
		If o\mesh = mesh Then cc = cc + 1
	Next
	Return cc
End Function

Function AddSound%(file$,loop%=1,Del=0,Wait=0,position%=0)
	d.Sound = New Sound
	d\Num = SOUND_COUNT% + 1
	d\DelayTm = del%
	d\DelaySt% = MilliSecs()+Wait
	d\Loop=loop%
	d\mesh% = position%
	SOUND_COUNT% = SOUND_COUNT% + 1
	
	If position% = 0
		d\Sound% = LoadSound(file$)
	Else
		d\Sound% = Load3DSound (file$)
	EndIf
	
	Return d\Num%
End Function

Function StopSound(Num%)
	For d.Sound = Each Sound
		If d\Num = num%
			StopChannel d\Channel
			FreeSound d\Sound
			Delete d
			Return
		EndIf
	Next
End Function