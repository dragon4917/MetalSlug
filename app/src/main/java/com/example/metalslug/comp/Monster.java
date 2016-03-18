package com.example.metalslug.comp;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.example.metalslug.game.Graphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iceberg on 2016/3/17.
 * 怪物类
 */
public class Monster {

    // 定义代表怪物类型的常量（如果程序还需要增加更多怪物，只需要在此处添加常量即可）
    public static final int TYPE_BOMB = 1;
    public static final int TYPE_FLY = 2;
    public static final int TYPE_MAN = 3;
    // 定义怪物类型的成员变量
    private int type = TYPE_BOMB;
    // 定义怪物 x、y 坐标的成员变量
    private int x = 0;
    private int y = 0;
    // 定义怪物是否已经死亡的旗标
    private boolean isDie = false;
    // 绘制怪物图片左上角的 x、y 坐标
    private int startX = 0;
    private int startY = 0;
    // 绘制怪物图片右下角的 x、y 坐标
    private int endX = 0;
    private int endY = 0;
    // 定义控制动画刷新速度的变量
    int drawCount = 0;
    // 定义当前正在绘制怪物动画的第几帧的变量
    private int drawIndex = 0;
    /*
        用于记录死亡动画只绘制一次，不需要重复绘制；
        每当怪物死亡时，该变量会被初始化等于死亡动画的总帧数；
        当怪物的死亡动画帧播放完成时，该变量的值变为0。
     */
    private int dieMaxDrawCount = Integer.MAX_VALUE;
    // 定义诖误射出的子弹
    private List<Bullet> bulletList = new ArrayList<>();

    // 怪物类构造器
    public Monster(int type) {
        this.type = type;
        // 计算怪物的随机 X 坐标
        x = ViewManager.SCREEN_WIDTH + Util.rand(ViewManager.SCREEN_WIDTH >> 1) - (ViewManager.SCREEN_WIDTH >> 2);
        // 计算怪物的随机 Y 坐标
        if (type == TYPE_BOMB || type == TYPE_MAN) {
        /*
            根据怪物类型来初始化 X、Y 坐标；
            如果怪物是炸弹或敌人，怪物的 Y 坐标与玩家控制的角色的 Y 坐标相同
         */
            y = Player.Y_DEFAULT;
        } else if (type == TYPE_FLY) {
            // 如果怪物是飞机，根据屏幕高度随机生成怪物的 Y 坐标
            y = ViewManager.SCREEN_HEIGHT * 50 / 100 - Util.rand((int) (ViewManager.scale * 100));
        }
    }

    // 绘制怪物的方法
    public void draw(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        switch (type) {
            // 死亡的怪物用死亡图片
            case TYPE_BOMB:
                drawAni(canvas, isDie ? ViewManager.bomb2Image : ViewManager.bombImage);
                break;
            case TYPE_FLY:
                drawAni(canvas, isDie ? ViewManager.flyDieImage : ViewManager.flyImage);
                break;
            case TYPE_MAN:
                drawAni(canvas, isDie ? ViewManager.manDieImage : ViewManager.manImage);
                break;
            default:
                break;
        }
    }

    // 根据怪物的动画帧图片来绘制怪物动画
    public void drawAni(Canvas canvas, Bitmap[] bitmapArr) {
        if (canvas == null) {
            return;
        }
        if (bitmapArr == null) {
            return;
        }
        // 如果怪物已经死亡，且没有播放过死亡动画（ DieMaxDrawCount 等于初始值表明未播放过死亡动画）
        if (isDie && dieMaxDrawCount == Integer.MAX_VALUE) {
            // 将 dieMaxDrawCount 设置为与死亡动画的总帧数相等
            dieMaxDrawCount = bitmapArr.length;
        }
        drawIndex = drawIndex % bitmapArr.length;
        // 获取当前绘制的动画帧对应的位图
        Bitmap bitmap = bitmapArr[drawIndex];
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        int drawX = x;
        // 对绘制怪物动画帧位图的 X 坐标进行微调
        if (isDie) {
            if (type == TYPE_BOMB) {
                drawX = x - (int) (ViewManager.scale * 50);
            } else if (type == TYPE_MAN) {
                drawX = x - (int) (ViewManager.scale * 50);
            }
        }
        // 对绘制怪物动画帧位图的 Y 坐标进行微调
        int drawY = y - bitmap.getHeight();
        // 绘制怪物动画帧的位图
        Graphics.drawMatrixImage(canvas, bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                Graphics.TRANS_NONE, drawX, drawY, 0, Graphics.TIMES_SCALE);
        startX = drawX;
        startY = drawY;
        endX = startX + bitmap.getWidth();
        endY = startY + bitmap.getHeight();
        drawCount++;
        // 后面的 6、4 用于控制人、飞机发射子弹的速度
        if (drawCount >= (type == TYPE_BOMB ? 6 : 4)) {
            // 如果怪物是人，只在第3帧才发射子弹
            if (type == TYPE_MAN && drawIndex == 2) {
                addBullet();
            }
            // 如果怪物是飞机，只在最后一帧才发射子弹
            if (type == TYPE_FLY && drawIndex == bitmapArr.length - 1) {
                addBullet();
            }
            drawIndex++;
            drawCount = 0;
        }
        // 每播放死亡动画的一帧， dieMaxDrawCount 减 1
        // 当 dieMaxDrawCount 等于 0 时，表明死亡动画播放完成， MosterManger 会删除该怪物
        if (isDie) {
            dieMaxDrawCount--;
        }
        // 绘制子弹
        drawBullet(canvas);
    }

    // 判断怪物是否被子弹打中的方法
    public boolean isHurt(int x, int y) {
        return x >= startX && x <= endX && y >= startY && y <= endY;
    }

    // 根据怪物类型获取子弹类型，不同怪物发射不同的子弹。0代表不发射子弹
    public int getBulletType() {
        switch (type) {
            case TYPE_BOMB:
                return 0;
            case TYPE_FLY:
                return Bullet.BULLET_TYPE_3;
            case TYPE_MAN:
                return Bullet.BULLET_TYPE_2;
            default:
                return 0;
        }
    }

    // 定义发射子弹的方法
    public void addBUllet() {
        int bulletType = getBulletType();
        if (bulletType <= 0) {
            return;
        }
        // 计算子弹的 X、Y 坐标
        int drawX = x;
        int drawY = y - (int) (ViewManager.scale * 60);
        // 如果怪物是飞机，重新计算飞机发射的子弹的 Y 坐标
        if (type == TYPE_FLY) {
            drawY = y - (int) (ViewManager.scale * 30);
        }
        // 创建子弹对象
        Bullet bullet = new Bullet(bulletType, drawX, drawY, Player.DIR_LEFT);
        // 将子弹添加到该怪物发射的子弹集合中
        bulletList.add(bullet);
    }


    // 更新角色的位置：将角色的X坐标减少shift距离（角色左移）
    // 更新所有子弹的位置：将所有子弹的X坐标减少shift距离（子弹左移）
    public void updateShift(int shift) {
        x -= shift;
        for (Bullet bullet : bulletList) {
            if (bullet == null) {
                continue;
            }
            bullet.setX(bullet.getX() - shift);
        }
    }

    // 绘制子弹的方法
    public void drawBullet(Canvas canvas) {
        // 定义一个deleteList集合，该集合保存所有需要删除的子弹
        List<Bullet> deleteList = new ArrayList<>();
        Bullet bullet = null;
        for (int i = 0; i < bulletList.size(); i++) {
            bullet = bulletList.get(i);
            if (bullet == null) {
                continue;
            }
            // 如果子弹已经越过屏幕
            if (bullet.getX() < 0 || bullet.getX() > ViewManager.SCREEN_WIDTH) {
                // 将需要清除的子弹添加到deleteList集合中
                deleteList.add(bullet);
            }
        }
        // 删除所有需要清除的子弹
        bulletList.removeAll(deleteList);  // ⑦
        // 定义代表子弹的位图
        Bitmap bitmap;
        // 遍历该怪物发射的所有子弹
        for (int i = 0; i < bulletList.size(); i++) {
            bullet = bulletList.get(i);
            if (bullet == null) {
                continue;
            }
            // 获取子弹对应的位图
            bitmap = bullet.getBitmap();
            if (bitmap == null) {
                continue;
            }
            // 子弹移动
            bullet.move();
            // 绘制子弹的位图
            Graphics.drawMatrixImage(canvas, bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), bullet.getDir() == Player.DIR_RIGHT ?
                            Graphics.TRANS_MIRROR : Graphics.TRANS_NONE,
                    bullet.getX(), bullet.getY(), 0, Graphics.TIMES_SCALE);
        }
    }


    // 判断子弹是否与玩家控制的角色碰撞（判断子弹是否打中角色）
    public void checkBullet() {
        // 定义一个delBulletList集合，该集合保存打中角色的子弹，它们将要被删除
        List<Bullet> delBulletList = new ArrayList<>();
        // 遍历所有子弹
        for (Bullet bullet : bulletList) {
            if (bullet == null || !bullet.isEffect()) {
                continue;
            }
            // 如果玩家控制的角色被子弹打到
            if (GameView.player.isHurt(bullet.getX(), bullet.getX()
                    , bullet.getY(), bullet.getY())) {
                // 子弹设为无效
                bullet.setEffect(false);
                // 将玩家的生命值减5
                GameView.player.setHp(GameView.player.getHp() - 5);
                // 将子弹添加到delBulletList集合中
                delBulletList.add(bullet);
            }
        }
        // 删除所有打中角色的子弹
        bulletList.removeAll(delBulletList);
    }

    // ----------下面是各成员变量的setter、getter方法----------
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public int getEndY() {
        return endY;
    }

    public void setEndY(int endY) {
        this.endY = endY;
    }

    public boolean isDie() {
        return isDie;
    }

    public void setDie(boolean isDie) {
        this.isDie = isDie;
    }

    public int getDieMaxDrawCount() {
        return dieMaxDrawCount;
    }

    public void setDieMaxDrawCount(int dieMaxDrawCount) {
        this.dieMaxDrawCount = dieMaxDrawCount;
    }
}
