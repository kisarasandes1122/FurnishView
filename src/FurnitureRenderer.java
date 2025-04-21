import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import java.awt.Color;

public class FurnitureRenderer {

    private TextureManager textureManager;

    public FurnitureRenderer(TextureManager textureManager) {
        if (textureManager == null) throw new IllegalArgumentException("TextureManager cannot be null");
        this.textureManager = textureManager;
    }

    public void drawFurniture(GL2 gl, Furniture furniture) {
        if (furniture == null) return;

        gl.glPushMatrix();

        Vector3f pos = furniture.getPosition();
        Vector3f rot = furniture.getRotation();
        gl.glTranslatef(pos.x, pos.y, pos.z);
        gl.glRotatef(rot.y, 0, 1, 0);
        gl.glRotatef(rot.x, 1, 0, 0);
        gl.glRotatef(rot.z, 0, 0, 1);

        gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_TEXTURE_BIT | GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT);

        Texture furnitureTex = textureManager.getTexture(gl, furniture.getTexturePath());
        boolean hasTexture = furnitureTex != null;

        if (hasTexture) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            furnitureTex.enable(gl);
            furnitureTex.bind(gl);
            DrawingUtils.setColor(gl, Color.WHITE);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            DrawingUtils.setColor(gl, furniture.getColor());
        }
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        drawRealisticFurniture(gl, furniture, hasTexture);

        if (hasTexture) {
            furnitureTex.disable(gl);
        }
        gl.glPopAttrib();

        gl.glPopMatrix();
    }

    private void drawRealisticFurniture(GL2 gl, Furniture furniture, boolean hasTexture) {
        String type = furniture.getType().toLowerCase();
        float w = furniture.getWidth();
        float h = furniture.getHeight();
        float d = furniture.getDepth();

        switch (type) {
            case "chair":           drawRealisticChair(gl, furniture, w, h, d, hasTexture); break;
            case "sofa":            drawRealisticSofa(gl, furniture, w, h, d, hasTexture); break;
            case "dining table":
            case "table":           drawRealisticTable(gl, furniture, w, h, d, hasTexture); break;
            case "side table":
            case "end table":       drawRealisticSideTable(gl, furniture, w, h, d, hasTexture); break;
            case "bed":
            case "queen bed":       drawRealisticBed(gl, furniture, w, h, d, hasTexture); break;
            case "bookshelf":       drawRealisticBookshelf(gl, furniture, w, h, d, hasTexture); break;
            case "armchair":        drawRealisticArmchair(gl, furniture, w, h, d, hasTexture); break;
            case "dining chair":    drawRealisticDiningChair(gl, furniture, w, h, d, hasTexture); break;
            case "office chair":    drawRealisticOfficeChair(gl, furniture, w, h, d, hasTexture); break;
            case "stool":           drawRealisticStool(gl, furniture, w, h, d, hasTexture); break;
            case "bench":           drawRealisticBench(gl, furniture, w, h, d, hasTexture); break;
            case "recliner":        drawRealisticRecliner(gl, furniture, w, h, d, hasTexture); break;
            case "ottoman":         drawRealisticOttoman(gl, furniture, w, h, d, hasTexture); break;
            case "coffee table":    drawRealisticCoffeeTable(gl, furniture, w, h, d, hasTexture); break;
            case "desk":            drawRealisticDesk(gl, furniture, w, h, d, hasTexture); break;
            case "console table":   drawRealisticConsoleTable(gl, furniture, w, h, d, hasTexture); break;
            case "wardrobe":        drawRealisticWardrobe(gl, furniture, w, h, d, hasTexture); break;
            case "dresser":         drawRealisticDresser(gl, furniture, w, h, d, hasTexture); break;
            case "filing cabinet":  drawRealisticFilingCabinet(gl, furniture, w, h, d, hasTexture); break;
            case "tv stand":        drawRealisticTvStand(gl, furniture, w, h, d, hasTexture); break;
            case "chest of drawers":drawRealisticChestOfDrawers(gl, furniture, w, h, d, hasTexture); break;
            case "twin bed":        drawRealisticBed(gl, furniture, w, h, d, hasTexture); break;
            case "king bed":        drawRealisticBed(gl, furniture, w, h, d, hasTexture); break;
            case "bunk bed":        drawRealisticBunkBed(gl, furniture, w, h, d, hasTexture); break;
            case "murphy bed":      drawRealisticMurphyBed(gl, furniture, w, h, d, hasTexture); break;
            case "headboard":       drawRealisticHeadboard(gl, furniture, w, h, d, hasTexture); break;
            case "crib":            drawRealisticCrib(gl, furniture, w, h, d, hasTexture); break;
            case "chaise lounge":   drawRealisticChaiseLounge(gl, furniture, w, h, d, hasTexture); break;
            case "futon":           drawRealisticFuton(gl, furniture, w, h, d, hasTexture); break;
            default:
                gl.glPushMatrix();
                gl.glTranslatef(0, h / 2f, 0);
                DrawingUtils.drawBox(gl, w, h, d, hasTexture);
                gl.glPopMatrix();
                break;
        }
    }

    private void drawRealisticChair(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float legRatio = 0.08f;
        float legThickness = Math.min(w, d) * legRatio;
        float legHeight = h * 0.42f;
        float seatThickness = h * 0.10f;
        float seatY = legHeight;
        float backHeight = h * 0.55f;
        float backThickness = legThickness * 1.1f;
        float armRestHeight = h * 0.22f;
        float armRestThickness = legThickness * 1.2f;
        float armRestTopY = seatY + seatThickness + armRestHeight;
        float armRestStartY = seatY + seatThickness;

        gl.glPushMatrix();
        gl.glTranslatef(0, seatY + seatThickness / 2f, 0);
        DrawingUtils.drawBox(gl, w, seatThickness, d, hasTexture);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0, seatY + seatThickness, -d / 2f + backThickness);
        gl.glRotatef(-8.0f, 1, 0, 0);
        gl.glTranslatef(0, backHeight / 2f, 0);
        DrawingUtils.drawBox(gl, w, backHeight, backThickness, hasTexture);
        gl.glPopMatrix();

        float armRestDepth = d * 0.85f;
        float armRestFrontOffset = (d - armRestDepth) / 2f;

        gl.glPushMatrix();
        gl.glTranslatef(w/2f - armRestThickness/2f, armRestStartY + armRestHeight/2f, -armRestFrontOffset);
        DrawingUtils.drawBox(gl, armRestThickness, armRestHeight, armRestDepth, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-w/2f + armRestThickness/2f, armRestStartY + armRestHeight/2f, -armRestFrontOffset);
        DrawingUtils.drawBox(gl, armRestThickness, armRestHeight, armRestDepth, hasTexture);
        gl.glPopMatrix();

        float legX = w/2f - legThickness/2f;
        float legZInset = legThickness * 0.2f;
        float legZFront = d/2f - legThickness/2f - legZInset;
        float legZBack = -d/2f + legThickness/2f + legZInset;

        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f,  legZFront);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f,  legZFront);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f,  legZBack);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f,  legZBack);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticTable(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float legThickness = Math.min(w, d) * 0.06f;
        float topThickness = h * 0.07f;
        float apronHeight = h * 0.08f;
        float legHeight = h - topThickness;
        float legTopY = legHeight;
        float apronTopY = legTopY;
        float apronBottomY = legTopY - apronHeight;

        gl.glPushMatrix();
        gl.glTranslatef(0, h - topThickness/2f, 0);
        DrawingUtils.drawBox(gl, w, topThickness, d, hasTexture);
        gl.glPopMatrix();

        float legInset = legThickness * 0.5f;
        float legX = w/2f - legThickness/2f - legInset;
        float legZ = d/2f - legThickness/2f - legInset;
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();

        float apronWidth = w - 2*legInset - 2*legThickness;
        float apronDepth = d - 2*legInset - 2*legThickness;
        float apronThickness = legThickness * 0.5f;
        gl.glPushMatrix();
        gl.glTranslatef(0, apronBottomY + apronHeight/2f, d/2f - legInset - legThickness/2f);
        DrawingUtils.drawBox(gl, apronWidth, apronHeight, apronThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, apronBottomY + apronHeight/2f, -d/2f + legInset + legThickness/2f);
        DrawingUtils.drawBox(gl, apronWidth, apronHeight, apronThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(w/2f - legInset - legThickness/2f, apronBottomY + apronHeight/2f, 0);
        DrawingUtils.drawBox(gl, apronThickness, apronHeight, apronDepth, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-w/2f + legInset + legThickness/2f, apronBottomY + apronHeight/2f, 0);
        DrawingUtils.drawBox(gl, apronThickness, apronHeight, apronDepth, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticSofa(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float feetHeight = h * 0.04f;
        float baseHeight = h * 0.18f;
        float baseActualHeight = baseHeight;
        float baseY = feetHeight;
        float seatCushionHeight = h * 0.20f;
        float seatCushionY = baseY + baseActualHeight;
        float armRestHeightAboveSeat = h * 0.20f;
        float armRestThickness = w * 0.10f;
        float armRestY = seatCushionY;
        float armRestTotalHeight = seatCushionHeight + armRestHeightAboveSeat;
        float backStructureHeight = armRestTotalHeight;
        float backStructureThickness = d * 0.15f;
        float backStructureY = baseY + baseActualHeight;
        float backCushionHeight = h * 0.40f;
        float backCushionThickness = backStructureThickness * 1.2f;
        float backCushionY = seatCushionY + seatCushionHeight;

        gl.glPushMatrix();
        gl.glTranslatef(0, baseY + baseActualHeight/2f, 0);
        DrawingUtils.drawBox(gl, w, baseActualHeight, d, hasTexture);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0, backStructureY + backStructureHeight/2f, -d/2f + backStructureThickness/2f);
        DrawingUtils.drawBox(gl, w, backStructureHeight, backStructureThickness, hasTexture);
        gl.glPopMatrix();

        int numSeatCushions = (w > 1.8f) ? 3 : 2;
        float totalSeatCushionWidth = w - 2 * armRestThickness;
        float seatCushionGap = totalSeatCushionWidth * 0.015f;
        float seatCushionWidth = (totalSeatCushionWidth - (numSeatCushions - 1) * seatCushionGap) / numSeatCushions;
        float seatCushionDepth = d - backStructureThickness - (d * 0.05f);
        float seatCushionStartZ = -d/2f + backStructureThickness + seatCushionDepth/2f;
        float seatCushionStartX = -totalSeatCushionWidth/2f + seatCushionWidth/2f;

        for (int i = 0; i < numSeatCushions; i++) {
            float currentX = seatCushionStartX + i * (seatCushionWidth + seatCushionGap);
            gl.glPushMatrix();
            gl.glTranslatef(currentX, seatCushionY + seatCushionHeight/2f, seatCushionStartZ);
            DrawingUtils.drawBox(gl, seatCushionWidth, seatCushionHeight, seatCushionDepth, hasTexture);
            gl.glPopMatrix();
        }

        int numBackCushions = numSeatCushions;
        float totalBackCushionWidth = w - 2 * armRestThickness;
        float backCushionGap = totalBackCushionWidth * 0.015f;
        float backCushionWidth = (totalBackCushionWidth - (numBackCushions - 1) * backCushionGap) / numBackCushions;
        float backCushionStartZ = -d/2f + backStructureThickness + backCushionThickness/2f;
        float backCushionStartX = -totalBackCushionWidth/2f + backCushionWidth/2f;

        for (int i = 0; i < numBackCushions; i++) {
            float currentX = backCushionStartX + i * (backCushionWidth + backCushionGap);
            gl.glPushMatrix();
            gl.glTranslatef(currentX, backCushionY, backCushionStartZ - backCushionThickness/2f);
            gl.glRotatef(-10.0f, 1, 0, 0);
            gl.glTranslatef(0, backCushionHeight/2f, backCushionThickness/2f);
            DrawingUtils.drawBox(gl, backCushionWidth, backCushionHeight, backCushionThickness, hasTexture);
            gl.glPopMatrix();
        }

        float armRestDepth = d * 0.9f;
        float armRestStartZ = -d/2f + armRestDepth/2f;
        gl.glPushMatrix();
        gl.glTranslatef(w/2f - armRestThickness/2f, armRestY + armRestTotalHeight/2f, armRestStartZ);
        DrawingUtils.drawBox(gl, armRestThickness, armRestTotalHeight, armRestDepth, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-w/2f + armRestThickness/2f, armRestY + armRestTotalHeight/2f, armRestStartZ);
        DrawingUtils.drawBox(gl, armRestThickness, armRestTotalHeight, armRestDepth, hasTexture);
        gl.glPopMatrix();

        float feetSize = feetHeight * 1.2f;
        float feetInset = armRestThickness;
        float feetX = w/2f - feetInset; float feetZ = d/2f - feetInset;
        gl.glPushMatrix();
        gl.glTranslatef( feetX, feetHeight/2f,  feetZ);
        DrawingUtils.drawBox(gl, feetSize, feetHeight, feetSize, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-feetX, feetHeight/2f,  feetZ);
        DrawingUtils.drawBox(gl, feetSize, feetHeight, feetSize, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef( feetX, feetHeight/2f, -feetZ);
        DrawingUtils.drawBox(gl, feetSize, feetHeight, feetSize, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-feetX, feetHeight/2f, -feetZ);
        DrawingUtils.drawBox(gl, feetSize, feetHeight, feetSize, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticBed(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float frameH = h * 0.25f;
        float mattressH = h * 0.22f;
        float duvetH = h * 0.08f;
        float pillowH = h * 0.15f;
        float pillowL = d * 0.25f;
        float pillowW = w * 0.35f;
        boolean twoPillows = (w > 1.2f);

        float mattressY = frameH;
        float duvetY = mattressY + mattressH;
        float pillowY = duvetY + duvetH * 0.5f;

        float totalMattressTop = mattressY + mattressH;
        float headboardH = h * 0.8f;
        float footboardH = Math.max(frameH + 0.05f, h*0.30f);
        float boardThickness = w * 0.05f;
        float sideRailH = frameH;
        float sideRailThickness = boardThickness * 0.9f;

        Color baseColor = furniture.getColor();
        Color detailColor = baseColor.darker();

        gl.glPushMatrix();
        gl.glTranslatef(0, headboardH/2f, -d/2f + boardThickness/2f);
        DrawingUtils.drawBox(gl, w, headboardH, boardThickness, hasTexture);
        gl.glPushMatrix();
        gl.glTranslatef(0, 0, boardThickness * 0.55f);
        DrawingUtils.setColor(gl, detailColor);
        DrawingUtils.drawBox(gl, w * 0.9f, headboardH * 0.85f, boardThickness * 0.1f, false);
        DrawingUtils.setColor(gl, baseColor);
        gl.glPopMatrix();
        gl.glPopMatrix();

        if (footboardH > 0.05f) {
            gl.glPushMatrix();
            gl.glTranslatef(0, footboardH/2f, d/2f - boardThickness/2f);
            DrawingUtils.drawBox(gl, w, footboardH, boardThickness, hasTexture);
            gl.glPushMatrix();
            gl.glTranslatef(0, 0, -boardThickness * 0.55f);
            DrawingUtils.setColor(gl, detailColor);
            DrawingUtils.drawBox(gl, w * 0.9f, footboardH * 0.85f, boardThickness * 0.1f, false);
            DrawingUtils.setColor(gl, baseColor);
            gl.glPopMatrix();
            gl.glPopMatrix();
        }

        float railLength = d - 2 * boardThickness;
        float railY = sideRailH / 2f;
        gl.glPushMatrix();
        gl.glTranslatef(w/2f - sideRailThickness/2f, railY, 0);
        DrawingUtils.drawBox(gl, sideRailThickness, sideRailH, railLength, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-w/2f + sideRailThickness/2f, railY, 0);
        DrawingUtils.drawBox(gl, sideRailThickness, sideRailH, railLength, hasTexture);
        gl.glPopMatrix();

        float mattressW = w - 2 * sideRailThickness - 0.04f;
        float mattressD = d - 2 * boardThickness - 0.04f;
        gl.glPushMatrix();
        gl.glTranslatef(0, mattressY + mattressH/2f, 0);
        DrawingUtils.drawBox(gl, mattressW, mattressH, mattressD, hasTexture);
        gl.glPopMatrix();

        float duvetW = mattressW + 0.06f;
        float duvetD = mattressD + 0.03f;
        gl.glPushMatrix();
        gl.glTranslatef(0, duvetY + duvetH/2f, 0);
        DrawingUtils.drawBox(gl, duvetW, duvetH, duvetD, hasTexture);
        gl.glPopMatrix();

        float pillowZ = -d/2f + boardThickness + pillowL/2f + 0.05f;
        if (twoPillows) {
            float pillowGap = w * 0.03f;
            float pillowXOffset = pillowW / 2f + pillowGap / 2f;
            gl.glPushMatrix();
            gl.glTranslatef(-pillowXOffset, pillowY + pillowH/2f, pillowZ);
            gl.glRotatef(5.0f, 1, 0, 0);
            DrawingUtils.drawBox(gl, pillowW, pillowH, pillowL, hasTexture);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslatef(pillowXOffset, pillowY + pillowH/2f, pillowZ);
            gl.glRotatef(5.0f, 1, 0, 0);
            DrawingUtils.drawBox(gl, pillowW, pillowH, pillowL, hasTexture);
            gl.glPopMatrix();
        } else {
            gl.glPushMatrix();
            gl.glTranslatef(0, pillowY + pillowH/2f, pillowZ);
            gl.glRotatef(5.0f, 1, 0, 0);
            DrawingUtils.drawBox(gl, pillowW * 1.8f, pillowH, pillowL, hasTexture);
            gl.glPopMatrix();
        }
    }

    private void drawRealisticBookshelf(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float sideThickness = w * 0.04f;
        float shelfThickness = h * 0.025f;
        float backThickness = sideThickness * 0.5f;
        float baseKickHeight = h * 0.05f;
        float topTrimHeight = h * 0.04f;
        int numShelves = 5;

        float innerW = w - 2 * sideThickness;
        float innerD = d - backThickness;
        float usableH = h - baseKickHeight - topTrimHeight;
        float shelfSpacing = (usableH - numShelves * shelfThickness) / (float) (numShelves - 1);
        if (numShelves <= 1) shelfSpacing = 0;

        gl.glPushMatrix();
        gl.glTranslatef(w/2f - sideThickness/2f, h/2f, 0);
        DrawingUtils.drawBox(gl, sideThickness, h, d, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-w/2f + sideThickness/2f, h/2f, 0);
        DrawingUtils.drawBox(gl, sideThickness, h, d, hasTexture);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0, h/2f, -d/2f + backThickness/2f);
        DrawingUtils.drawBox(gl, innerW, h, backThickness, hasTexture);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0, shelfThickness/2f, -backThickness/2f + innerD/2f);
        DrawingUtils.drawBox(gl, innerW, shelfThickness, innerD, hasTexture);
        gl.glPopMatrix();

        float currentShelfY = shelfThickness;
        for (int i = 1; i < numShelves -1; i++) {
            currentShelfY += shelfSpacing + shelfThickness;
            gl.glPushMatrix();
            gl.glTranslatef(0, currentShelfY - shelfThickness/2f, -backThickness/2f + innerD/2f);
            DrawingUtils.drawBox(gl, innerW, shelfThickness, innerD, hasTexture);
            gl.glPopMatrix();
        }

        gl.glPushMatrix();
        gl.glTranslatef(0, h - shelfThickness/2f, -backThickness/2f + innerD/2f);
        DrawingUtils.drawBox(gl, innerW, shelfThickness, innerD, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticSideTable(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float legThickness = Math.min(w, d) * 0.1f;
        float topThickness = h * 0.1f;
        float legHeight = h - topThickness;

        gl.glPushMatrix();
        gl.glTranslatef(0, h - topThickness/2f, 0);
        DrawingUtils.drawBox(gl, w, topThickness, d, hasTexture);
        gl.glPopMatrix();

        float legX = w/2f - legThickness/2f;
        float legZ = d/2f - legThickness/2f;
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticArmchair(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float legRatio = 0.1f;
        float legThickness = Math.min(w, d) * legRatio;
        float legHeight = h * 0.35f;
        float seatThickness = h * 0.18f;
        float seatY = legHeight;
        float backHeight = h * 0.60f;
        float backThickness = legThickness * 1.5f;
        float armRestHeight = h * 0.30f;
        float armRestThickness = legThickness * 1.8f;
        float armRestStartY = seatY + seatThickness * 0.5f;
        float armRestDepth = d * 0.8f;
        float armRestFrontOffset = (d - armRestDepth) / 2f;

        gl.glPushMatrix();
        gl.glTranslatef(0, seatY + seatThickness / 2f, 0);
        DrawingUtils.drawBox(gl, w, seatThickness, d, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, seatY + seatThickness, -d / 2f + backThickness);
        gl.glRotatef(-12.0f, 1, 0, 0);
        gl.glTranslatef(0, backHeight / 2f, 0);
        DrawingUtils.drawBox(gl, w, backHeight, backThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(w/2f - armRestThickness/2f, armRestStartY + armRestHeight/2f, -armRestFrontOffset);
        DrawingUtils.drawBox(gl, armRestThickness, armRestHeight, armRestDepth, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-w/2f + armRestThickness/2f, armRestStartY + armRestHeight/2f, -armRestFrontOffset);
        DrawingUtils.drawBox(gl, armRestThickness, armRestHeight, armRestDepth, hasTexture);
        gl.glPopMatrix();
        float legX = w/2f - armRestThickness/2f;
        float legZFront = d/2f - legThickness*1.5f;
        float legZBack = -d/2f + legThickness*1.5f;
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f,  legZFront);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f,  legZFront);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f,  legZBack);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f,  legZBack);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticDiningChair(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float legThickness = Math.min(w, d) * 0.07f;
        float legHeight = h * 0.45f;
        float seatThickness = h * 0.08f;
        float seatY = legHeight;
        float backHeight = h * 0.50f;
        float backThickness = legThickness * 0.8f;

        gl.glPushMatrix();
        gl.glTranslatef(0, seatY + seatThickness / 2f, 0);
        DrawingUtils.drawBox(gl, w, seatThickness, d, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, seatY + seatThickness, -d / 2f + backThickness * 0.5f);
        gl.glRotatef(-5.0f, 1, 0, 0);
        gl.glTranslatef(0, backHeight / 2f, 0);
        DrawingUtils.drawBox(gl, w*0.9f, backHeight, backThickness, hasTexture);
        gl.glPopMatrix();
        float legX = w/2f - legThickness/2f;
        float legZ = d/2f - legThickness/2f;
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticOfficeChair(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float baseHeight = 0.1f;
        float baseRadius = w * 0.4f;
        int numBaseLegs = 5;
        float stemHeight = h * 0.35f;
        float stemRadius = 0.04f;
        float seatThickness = h * 0.1f;
        float seatY = baseHeight + stemHeight;
        float backHeight = h * 0.5f;
        float backThickness = 0.06f;
        float armHeight = h * 0.18f;
        float armThickness = 0.05f;
        float armDepth = d * 0.5f;
        float armY = seatY + seatThickness;

        gl.glPushMatrix();
        gl.glTranslatef(0, baseHeight/2f, 0);
        DrawingUtils.drawBox(gl, stemRadius*2.5f, baseHeight, stemRadius*2.5f, hasTexture);
        gl.glPopMatrix();
        for (int i = 0; i < numBaseLegs; i++) {
            gl.glPushMatrix();
            gl.glRotatef(i * (360.0f / numBaseLegs), 0, 1, 0);
            gl.glTranslatef(baseRadius * 0.6f, baseHeight * 0.25f, 0);
            DrawingUtils.drawBox(gl, baseRadius * 0.8f, baseHeight * 0.5f, baseHeight * 0.8f, hasTexture);
            gl.glPopMatrix();
        }
        gl.glPushMatrix();
        gl.glTranslatef(0, baseHeight + stemHeight/2f, 0);
        DrawingUtils.drawBox(gl, stemRadius*2f, stemHeight, stemRadius*2f, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, seatY + seatThickness/2f, 0);
        DrawingUtils.drawBox(gl, w, seatThickness, d, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, seatY + seatThickness, -d/2f + backThickness);
        gl.glRotatef(-10f, 1, 0, 0);
        gl.glTranslatef(0, backHeight/2f, 0);
        DrawingUtils.drawBox(gl, w*0.9f, backHeight, backThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(w*0.4f, armY+armHeight/2f, -d*0.1f);
        DrawingUtils.drawBox(gl, armThickness, armHeight, armDepth, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-w*0.4f, armY+armHeight/2f, -d*0.1f);
        DrawingUtils.drawBox(gl, armThickness, armHeight, armDepth, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticStool(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float topThickness = h * 0.08f;
        float legThickness = w * 0.1f;
        float legHeight = h - topThickness;
        boolean fourLegs = true;

        gl.glPushMatrix();
        gl.glTranslatef(0, h - topThickness/2f, 0);
        DrawingUtils.drawBox(gl, w, topThickness, d, hasTexture);
        gl.glPopMatrix();
        if (fourLegs) {
            float legX = w/2f - legThickness/2f; float legZ = d/2f - legThickness/2f;
            gl.glPushMatrix();
            gl.glTranslatef( legX, legHeight/2f,  legZ);
            DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslatef(-legX, legHeight/2f,  legZ);
            DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslatef( legX, legHeight/2f, -legZ);
            DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslatef(-legX, legHeight/2f, -legZ);
            DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
            gl.glPopMatrix();
        } else {
            float pedestalHeight = h - topThickness;
            float pedestalRadius = w * 0.35f;
            gl.glPushMatrix();
            gl.glTranslatef(0, pedestalHeight/2f, 0);
            DrawingUtils.drawBox(gl, pedestalRadius*2f, pedestalHeight, pedestalRadius*2f, hasTexture);
            gl.glPopMatrix();
        }
    }

    private void drawRealisticBench(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float legThickness = Math.min(w, d) * 0.12f;
        float topThickness = h * 0.15f;
        float legHeight = h - topThickness;

        gl.glPushMatrix();
        gl.glTranslatef(0, h - topThickness/2f, 0);
        DrawingUtils.drawBox(gl, w, topThickness, d, hasTexture);
        gl.glPopMatrix();
        float legX = w/2f - legThickness/2f; float legZ = d/2f - legThickness/2f;
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticRecliner(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float legRatio = 0.12f;
        float legThickness = Math.min(w, d) * legRatio;
        float baseHeight = h * 0.40f;
        float seatThickness = h * 0.15f;
        float seatY = baseHeight * 0.6f;
        float backHeight = h * 0.65f;
        float backThickness = legThickness * 1.8f;
        float armRestHeight = h * 0.35f;
        float armRestThickness = legThickness * 2.2f;
        float armRestStartY = seatY;
        float armRestDepth = d * 0.9f;
        float armRestFrontOffset = (d - armRestDepth) / 2f;
        float footrestHeight = baseHeight * 0.8f;
        float footrestDepth = d * 0.4f;
        float footrestWidth = w * 0.7f;

        gl.glPushMatrix();
        gl.glTranslatef(0, baseHeight / 2f, 0);
        DrawingUtils.drawBox(gl, w, baseHeight, d, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, seatY + seatThickness*0.5f, -d / 2f + backThickness);
        gl.glRotatef(-25.0f, 1, 0, 0);
        gl.glTranslatef(0, backHeight / 2f, 0);
        DrawingUtils.drawBox(gl, w, backHeight, backThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(w/2f - armRestThickness/2f, armRestStartY + armRestHeight/2f, -armRestFrontOffset);
        DrawingUtils.drawBox(gl, armRestThickness, armRestHeight, armRestDepth, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-w/2f + armRestThickness/2f, armRestStartY + armRestHeight/2f, -armRestFrontOffset);
        DrawingUtils.drawBox(gl, armRestThickness, armRestHeight, armRestDepth, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, footrestHeight*0.5f, d/2f + footrestDepth*0.5f);
        DrawingUtils.drawBox(gl, footrestWidth, footrestHeight, footrestDepth, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticOttoman(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float feetHeight = h * 0.1f;
        float boxHeight = h - feetHeight;
        float feetSize = feetHeight * 0.8f;

        gl.glPushMatrix();
        gl.glTranslatef(0, feetHeight + boxHeight/2f, 0);
        DrawingUtils.drawBox(gl, w, boxHeight, d, hasTexture);
        gl.glPopMatrix();
        float feetX = w/2f - feetSize; float feetZ = d/2f - feetSize;
        gl.glPushMatrix();
        gl.glTranslatef( feetX, feetHeight/2f,  feetZ);
        DrawingUtils.drawBox(gl, feetSize, feetHeight, feetSize, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-feetX, feetHeight/2f,  feetZ);
        DrawingUtils.drawBox(gl, feetSize, feetHeight, feetSize, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef( feetX, feetHeight/2f, -feetZ);
        DrawingUtils.drawBox(gl, feetSize, feetHeight, feetSize, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-feetX, feetHeight/2f, -feetZ);
        DrawingUtils.drawBox(gl, feetSize, feetHeight, feetSize, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticCoffeeTable(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float legThickness = Math.min(w, d) * 0.08f;
        float topThickness = h * 0.18f;
        float legHeight = h - topThickness;
        float legInset = legThickness * 1.5f;

        gl.glPushMatrix();
        gl.glTranslatef(0, h - topThickness/2f, 0);
        DrawingUtils.drawBox(gl, w, topThickness, d, hasTexture);
        gl.glPopMatrix();
        float legX = w/2f - legThickness/2f - legInset;
        float legZ = d/2f - legThickness/2f - legInset;
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticDesk(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float legThickness = Math.min(w, d) * 0.07f;
        float topThickness = h * 0.06f;
        float legHeight = h - topThickness;
        float drawerUnitWidth = w * 0.3f;
        float drawerUnitHeight = legHeight * 0.8f;
        boolean hasDrawerUnit = true;

        gl.glPushMatrix();
        gl.glTranslatef(0, h - topThickness/2f, 0);
        DrawingUtils.drawBox(gl, w, topThickness, d, hasTexture);
        gl.glPopMatrix();
        if (hasDrawerUnit) {
            float unitX = w/2f - drawerUnitWidth/2f;
            gl.glPushMatrix();
            gl.glTranslatef(unitX, drawerUnitHeight/2f, 0);
            DrawingUtils.drawBox(gl, drawerUnitWidth, drawerUnitHeight, d*0.9f, hasTexture);
            gl.glPopMatrix();
            float legX = -w/2f + legThickness/2f;
            float legZ = d/2f - legThickness/2f;
            gl.glPushMatrix();
            gl.glTranslatef(legX, legHeight/2f,  legZ);
            DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslatef(legX, legHeight/2f, -legZ);
            DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
            gl.glPopMatrix();
        } else {
            float legX = w/2f - legThickness*1.5f; float legZ = d/2f - legThickness*1.5f;
            gl.glPushMatrix();
            gl.glTranslatef( legX, legHeight/2f,  legZ);
            DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslatef(-legX, legHeight/2f,  legZ);
            DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslatef( legX, legHeight/2f, -legZ);
            DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslatef(-legX, legHeight/2f, -legZ);
            DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
            gl.glPopMatrix();
        }
    }

    private void drawRealisticConsoleTable(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float legThickness = Math.min(w, d) * 0.08f;
        float topThickness = h * 0.06f;
        float legHeight = h - topThickness;

        gl.glPushMatrix();
        gl.glTranslatef(0, h - topThickness/2f, 0);
        DrawingUtils.drawBox(gl, w, topThickness, d, hasTexture);
        gl.glPopMatrix();
        float legX = w/2f - legThickness/2f; float legZ = d/2f - legThickness/2f;
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f,  legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef( legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-legX, legHeight/2f, -legZ);
        DrawingUtils.drawBox(gl, legThickness, legHeight, legThickness, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticWardrobe(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float doorThickness = 0.02f;
        float handleSize = 0.03f;

        gl.glPushMatrix();
        gl.glTranslatef(0, h/2f, 0);
        DrawingUtils.drawBox(gl, w, h, d, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-w*0.25f, h*0.5f, d/2f + doorThickness*0.5f);
        DrawingUtils.drawBox(gl, handleSize, handleSize*3f, doorThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(w*0.25f, h*0.5f, d/2f + doorThickness*0.5f);
        DrawingUtils.drawBox(gl, handleSize, handleSize*3f, doorThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushAttrib(GL2.GL_LIGHTING_BIT | GL2.GL_LINE_BIT | GL2.GL_CURRENT_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        DrawingUtils.setColor(gl, Color.BLACK);
        gl.glLineWidth(1.0f);
        gl.glBegin(GL.GL_LINES);
        gl.glVertex3f(0, 0, d/2f+0.001f);
        gl.glVertex3f(0, h, d/2f+0.001f);
        gl.glEnd();
        DrawingUtils.setColor(gl, furniture.getColor());
        gl.glPopAttrib();
    }

    private void drawRealisticDresser(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        int numDrawersH = (w > 1.2f) ? 3 : 2;
        int numDrawersV = (h > 0.9f) ? 4 : 3;
        float frameThickness = 0.02f;
        float drawerGap = 0.01f;
        float handleSize = 0.02f;

        gl.glPushMatrix();
        gl.glTranslatef(0, h/2f, 0);
        DrawingUtils.drawBox(gl, w, h, d, hasTexture);
        gl.glPopMatrix();

        float usableW = w - 2 * frameThickness - (numDrawersH - 1) * drawerGap;
        float usableH = h - 2 * frameThickness - (numDrawersV - 1) * drawerGap;
        float drawerW = usableW / numDrawersH;
        float drawerH = usableH / numDrawersV;
        float startX = -w/2f + frameThickness + drawerW/2f;
        float startY = frameThickness + drawerH/2f;

        gl.glPushAttrib(GL2.GL_LIGHTING_BIT | GL2.GL_LINE_BIT | GL2.GL_CURRENT_BIT);
        gl.glDisable(GL2.GL_LIGHTING);

        for (int i = 0; i < numDrawersH; i++) {
            for (int j = 0; j < numDrawersV; j++) {
                float currentX = startX + i * (drawerW + drawerGap);
                float currentY = startY + j * (drawerH + drawerGap);

                DrawingUtils.setColor(gl, Color.DARK_GRAY);
                gl.glPushMatrix();
                gl.glTranslatef(currentX, currentY, d/2f + handleSize*0.5f);
                DrawingUtils.drawBox(gl, handleSize*3f, handleSize, handleSize, false);
                gl.glPopMatrix();

                DrawingUtils.setColor(gl, Color.BLACK);
                gl.glLineWidth(1.0f);
                gl.glBegin(GL.GL_LINES);
                if (j > 0) {
                    float lineY = currentY - drawerH*0.5f - drawerGap*0.5f;
                    gl.glVertex3f(currentX-drawerW*0.5f, lineY, d/2f+0.001f);
                    gl.glVertex3f(currentX+drawerW*0.5f, lineY, d/2f+0.001f);
                }
                if (i > 0) {
                    float lineX = currentX - drawerW*0.5f - drawerGap*0.5f;
                    gl.glVertex3f(lineX, currentY-drawerH*0.5f, d/2f+0.001f);
                    gl.glVertex3f(lineX, currentY+drawerH*0.5f, d/2f+0.001f);
                }
                gl.glEnd();
            }
        }
        DrawingUtils.setColor(gl, furniture.getColor());
        gl.glPopAttrib();
    }


    private void drawRealisticFilingCabinet(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        int numDrawers = Math.max(2, (int)(h / 0.25f));
        float drawerH = h / numDrawers;
        float handleSize = 0.03f;
        float handleW = w * 0.6f;

        gl.glPushMatrix();
        gl.glTranslatef(0, h/2f, 0);
        DrawingUtils.drawBox(gl, w, h, d, hasTexture);
        gl.glPopMatrix();

        gl.glPushAttrib(GL2.GL_LIGHTING_BIT | GL2.GL_LINE_BIT | GL2.GL_CURRENT_BIT);
        gl.glDisable(GL2.GL_LIGHTING);

        for(int i=0; i < numDrawers; i++) {
            float currentY = (i + 0.5f) * drawerH;
            DrawingUtils.setColor(gl, Color.DARK_GRAY);
            gl.glPushMatrix();
            gl.glTranslatef(0, currentY, d/2f+handleSize*0.5f);
            DrawingUtils.drawBox(gl, handleW, handleSize, handleSize, false);
            gl.glPopMatrix();

            if (i < numDrawers - 1) {
                DrawingUtils.setColor(gl, Color.BLACK);
                gl.glLineWidth(1.0f);
                gl.glBegin(GL.GL_LINES);
                float lineY = (i+1)*drawerH;
                gl.glVertex3f(-w/2f, lineY, d/2f+0.001f);
                gl.glVertex3f( w/2f, lineY, d/2f+0.001f);
                gl.glEnd();
            }
        }
        DrawingUtils.setColor(gl, furniture.getColor());
        gl.glPopAttrib();
    }

    private void drawRealisticTvStand(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float topThickness = h * 0.1f;
        float shelfHeight = h * 0.4f;
        float shelfThickness = h * 0.08f;
        float baseHeight = Math.max(0.05f, h - topThickness - shelfHeight - shelfThickness);

        gl.glPushMatrix();
        gl.glTranslatef(0, h - topThickness/2f, 0);
        DrawingUtils.drawBox(gl, w, topThickness, d, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, baseHeight + shelfHeight + shelfThickness/2f, 0);
        DrawingUtils.drawBox(gl, w*0.95f, shelfThickness, d*0.9f, hasTexture);
        gl.glPopMatrix();
        float sideW = w*0.05f;
        float sideH = baseHeight+shelfHeight + shelfThickness;
        float sideY = sideH / 2f;
        gl.glPushMatrix();
        gl.glTranslatef(w/2f-sideW/2f, sideY, 0);
        DrawingUtils.drawBox(gl, sideW, sideH, d, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-w/2f+sideW/2f, sideY, 0);
        DrawingUtils.drawBox(gl, sideW, sideH, d, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, sideY, -d/2f+0.01f);
        DrawingUtils.drawBox(gl, w-2*sideW, sideH, 0.02f, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticChestOfDrawers(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        drawRealisticDresser(gl, furniture, w, h, d, hasTexture);
    }

    private void drawRealisticBunkBed(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float postThickness = 0.08f;
        float bedFrameH = 0.15f;
        float mattressH = 0.12f;
        float lowerBedFrameTopY = h * 0.2f;
        float upperBedFrameTopY = h * 0.75f;
        float lowerMattressY = lowerBedFrameTopY;
        float upperMattressY = upperBedFrameTopY;
        float spaceBetween = upperBedFrameTopY - lowerBedFrameTopY - bedFrameH;
        float ladderWidth = w * 0.3f;

        float postX = w/2f - postThickness/2f; float postZ = d/2f - postThickness/2f;
        gl.glPushMatrix();
        gl.glTranslatef( postX, h/2f,  postZ);
        DrawingUtils.drawBox(gl, postThickness, h, postThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-postX, h/2f,  postZ);
        DrawingUtils.drawBox(gl, postThickness, h, postThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef( postX, h/2f, -postZ);
        DrawingUtils.drawBox(gl, postThickness, h, postThickness, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-postX, h/2f, -postZ);
        DrawingUtils.drawBox(gl, postThickness, h, postThickness, hasTexture);
        gl.glPopMatrix();

        float frameW = w - 2*postThickness; float frameD = d - 2*postThickness;
        gl.glPushMatrix();
        gl.glTranslatef(0, lowerBedFrameTopY - bedFrameH/2f, 0);
        DrawingUtils.drawBox(gl, frameW, bedFrameH, frameD, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, upperBedFrameTopY - bedFrameH/2f, 0);
        DrawingUtils.drawBox(gl, frameW, bedFrameH, frameD, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, lowerMattressY + mattressH/2f, 0);
        DrawingUtils.drawBox(gl, frameW*0.95f, mattressH, frameD*0.95f, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, upperMattressY + mattressH/2f, 0);
        DrawingUtils.drawBox(gl, frameW*0.95f, mattressH, frameD*0.95f, hasTexture);
        gl.glPopMatrix();

        float ladderX = -w/2f;
        float ladderStepH = 0.03f; float ladderRailT = 0.04f; int numSteps = 4;
        float ladderBottomY = lowerBedFrameTopY - bedFrameH / 2f;
        float ladderTopY = upperBedFrameTopY - bedFrameH / 2f;
        float ladderActualH = ladderTopY - ladderBottomY;
        float stepSpacing = ladderActualH / (numSteps+1);

        gl.glPushMatrix();
        gl.glTranslatef(ladderX, ladderBottomY + ladderActualH/2f, d/2f);
        DrawingUtils.drawBox(gl, ladderRailT, ladderActualH, ladderRailT, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(ladderX+ladderWidth, ladderBottomY + ladderActualH/2f, d/2f);
        DrawingUtils.drawBox(gl, ladderRailT, ladderActualH, ladderRailT, hasTexture);
        gl.glPopMatrix();
        for(int i=1; i<=numSteps; i++) {
            gl.glPushMatrix();
            gl.glTranslatef(ladderX+ladderWidth/2f, ladderBottomY + i*stepSpacing, d/2f);
            DrawingUtils.drawBox(gl, ladderWidth, ladderStepH, ladderRailT*1.5f, hasTexture);
            gl.glPopMatrix();
        }
    }


    private void drawRealisticMurphyBed(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float cabinetThickness = 0.1f;
        float bedMattressH = h;
        float bedLength = d;
        float cabinetHeight = bedLength + 0.1f;
        float cabinetWidth = w + 0.1f;

        gl.glPushMatrix();
        gl.glTranslatef(0, cabinetHeight/2f, -bedLength/2f - cabinetThickness/2f);
        DrawingUtils.drawBox(gl, cabinetWidth, cabinetHeight, cabinetThickness, hasTexture);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0, bedMattressH/2f, 0);
        DrawingUtils.drawBox(gl, w, bedMattressH, bedLength, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticHeadboard(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        Color baseColor = furniture.getColor();
        Color detailColor = baseColor.darker();

        gl.glPushMatrix();
        gl.glTranslatef(0, h/2f, 0);
        DrawingUtils.drawBox(gl, w, h, d, hasTexture);
        gl.glPushMatrix();
        gl.glTranslatef(0, 0, d*0.6f);
        DrawingUtils.setColor(gl, detailColor);
        DrawingUtils.drawBox(gl, w*0.9f, h*0.85f, d*0.1f, false);
        DrawingUtils.setColor(gl, baseColor);
        gl.glPopMatrix();
        gl.glPopMatrix();
    }

    private void drawRealisticCrib(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float postThickness = 0.05f;
        float railThickness = 0.03f;
        float topRailY = h * 0.9f;
        float bottomRailY = h * 0.1f;
        float railHeight = railThickness * 1.5f;
        float spindleHeight = topRailY - railHeight - bottomRailY;
        float spindleY = bottomRailY + spindleHeight/2f;
        float baseHeight = h * 0.3f;
        float mattressH = 0.1f;
        int numSpindlesW = Math.max(2, (int)(w / 0.12f));
        int numSpindlesD = Math.max(2, (int)(d / 0.12f));

        float postX = w/2f - postThickness/2f; float postZ = d/2f - postThickness/2f;
        gl.glPushMatrix(); gl.glTranslatef( postX, h/2f,  postZ); DrawingUtils.drawBox(gl, postThickness, h, postThickness, hasTexture); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(-postX, h/2f,  postZ); DrawingUtils.drawBox(gl, postThickness, h, postThickness, hasTexture); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef( postX, h/2f, -postZ); DrawingUtils.drawBox(gl, postThickness, h, postThickness, hasTexture); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(-postX, h/2f, -postZ); DrawingUtils.drawBox(gl, postThickness, h, postThickness, hasTexture); gl.glPopMatrix();

        float railLenW = w - 2*postThickness;
        float railLenD = d - 2*postThickness;
        gl.glPushMatrix(); gl.glTranslatef(0, topRailY-railHeight/2f, postZ); DrawingUtils.drawBox(gl, railLenW, railHeight, railThickness, hasTexture); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(0, bottomRailY+railHeight/2f, postZ); DrawingUtils.drawBox(gl, railLenW, railHeight, railThickness, hasTexture); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(0, topRailY-railHeight/2f, -postZ); DrawingUtils.drawBox(gl, railLenW, railHeight, railThickness, hasTexture); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(0, bottomRailY+railHeight/2f, -postZ); DrawingUtils.drawBox(gl, railLenW, railHeight, railThickness, hasTexture); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(postX, topRailY-railHeight/2f, 0); DrawingUtils.drawBox(gl, railThickness, railHeight, railLenD, hasTexture); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(postX, bottomRailY+railHeight/2f, 0); DrawingUtils.drawBox(gl, railThickness, railHeight, railLenD, hasTexture); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(-postX, topRailY-railHeight/2f, 0); DrawingUtils.drawBox(gl, railThickness, railHeight, railLenD, hasTexture); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(-postX, bottomRailY+railHeight/2f, 0); DrawingUtils.drawBox(gl, railThickness, railHeight, railLenD, hasTexture); gl.glPopMatrix();


        float spindleT = railThickness * 0.8f;
        float spindleSpacingW = railLenW / (numSpindlesW + 1);
        for(int i=1; i <= numSpindlesW; i++) {
            float spindleX = -railLenW/2f + i*spindleSpacingW;
            gl.glPushMatrix();
            gl.glTranslatef(spindleX, spindleY, postZ);
            DrawingUtils.drawBox(gl, spindleT, spindleHeight, spindleT, hasTexture);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslatef(spindleX, spindleY, -postZ);
            DrawingUtils.drawBox(gl, spindleT, spindleHeight, spindleT, hasTexture);
            gl.glPopMatrix();
        }

        gl.glPushMatrix();
        gl.glTranslatef(0, baseHeight, 0);
        DrawingUtils.drawBox(gl, railLenW, 0.04f, railLenD, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, baseHeight+0.04f+mattressH/2f, 0);
        DrawingUtils.drawBox(gl, railLenW*0.95f, mattressH, railLenD*0.95f, hasTexture);
        gl.glPopMatrix();

    }

    private void drawRealisticChaiseLounge(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        float baseH = h * 0.3f;
        float cushionH = h * 0.2f;
        float backStartY = baseH + cushionH;
        float backMaxH = h * 0.5f;
        float backMinH = 0;
        float backThickness = w * 0.15f;
        float armH = h * 0.3f;
        float armD = d * 0.2f;
        float armY = baseH;

        gl.glPushMatrix();
        gl.glTranslatef(0, baseH/2f, 0);
        DrawingUtils.drawBox(gl, w, baseH, d, hasTexture);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, baseH + cushionH/2f, 0);
        DrawingUtils.drawBox(gl, w*0.95f, cushionH, d*0.95f, hasTexture);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(w/2f - backThickness/2f, backStartY, 0);
        float avgBackH = (backMaxH + backMinH) / 2.0f;
        float angle = (float) Math.toDegrees(Math.atan((backMaxH - backMinH) / d));
        gl.glRotatef(-15f, 1, 0, 0);
        gl.glTranslatef(0, avgBackH / 2f, 0);
        DrawingUtils.drawBox(gl, backThickness, avgBackH, d*0.9f, hasTexture);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(w/2f - backThickness - armD/2f, armY + armH/2f, 0);
        DrawingUtils.drawBox(gl, armD, armH, d*0.8f, hasTexture);
        gl.glPopMatrix();
    }

    private void drawRealisticFuton(GL2 gl, Furniture furniture, float w, float h, float d, boolean hasTexture) {
        drawRealisticSofa(gl, furniture, w, h, d, hasTexture);
    }

}