package com.example.PlanetFormationDemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Physics + game state; produces snapshots and events for rendering/UI.
 */
public class Simulation {
    private static final float G = 0.0008f;
    private static final float GRAVITY_SOFTENING = 3000f;
    private static final float DRAG = 0.9999f;
    private static final int CELL_SIZE = 12;
    private static final float PLANET_GRAVITY_SCALE = 0.18f;
    private static final int MAX_GIANTS = 16;

    private int width;
    private int height;
    private final List<Body> bodies = new ArrayList<>();
    private final List<List<Body>> grid;
    private final int gridCols;
    private final int gridRows;
    private final Random random = new Random();
    private final CopyOnWriteArrayList<SimulationListener> listeners = new CopyOnWriteArrayList<>();

    private volatile boolean running = false;
    private ExecutorService physicsExecutor;
    private ScheduledExecutorService scheduler;
    private long lastMergeChimeMs = 0L;
    private int physicsTick = 0;
    private boolean autoCometsEnabled = true;

    public Simulation(int width, int height) {
        this.width = width;
        this.height = height;
        this.gridCols = width / CELL_SIZE + 2;
        this.gridRows = height / CELL_SIZE + 2;
        grid = new ArrayList<>(gridCols * gridRows);
        for (int i = 0; i < gridCols * gridRows; i++) {
            grid.add(new ArrayList<>(24));
        }
        initBodies();
    }

    public void addListener(SimulationListener l) {
        listeners.add(l);
    }

    public void start() {
        if (running) return;
        running = true;
        physicsExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Physics"));
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "SimScheduler"));
        physicsExecutor.submit(this::loop);
        scheduleAutoComet();
    }

    public void stop() {
        running = false;
        if (physicsExecutor != null) physicsExecutor.shutdownNow();
        if (scheduler != null) scheduler.shutdownNow();
    }

    private void loop() {
        while (running) {
            long start = System.nanoTime();
            step();
            long elapsed = (System.nanoTime() - start) / 1_000_000L;
            long sleep = Math.max(2, 16 - elapsed);
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void initBodies() {
        bodies.clear();
        Body sun = new Body();
        sun.x = width / 2f;
        sun.y = height / 2f;
        sun.mass = 1_200_000f;
        sun.radius = 26f;
        sun.color = new java.awt.Color(255, 160, 40);
        sun.isSun = true;
        sun.particleCount = 0;
        bodies.add(sun);

        float minR = Math.min(width, height) * 0.10f;
        float maxR = Math.min(width, height) * 0.48f;
        for (int i = 0; i < 80_000; i++) {
            float r = minR + random.nextFloat() * (maxR - minR);
            double angle = random.nextDouble() * Math.PI * 2;
            float x = sun.x + (float) (Math.cos(angle) * r);
            float y = sun.y + (float) (Math.sin(angle) * r);
            float mass = 0.25f + random.nextFloat() * 0.9f;
            float speed = (float) Math.sqrt((G * sun.mass) / r);
            float tangentialScale = 0.90f + random.nextFloat() * 0.18f;
            float vx = (float) (-Math.sin(angle) * speed) * tangentialScale + (random.nextFloat() - 0.5f) * 0.12f;
            float vy = (float) (Math.cos(angle) * speed) * tangentialScale + (random.nextFloat() - 0.5f) * 0.12f;
            addBody(x, y, vx, vy, mass, false);
        }
    }

    public void resetForSize(int w, int h) {
        synchronized (bodies) {
            bodies.clear();
            // rebuild with new width/height centers
            Simulation fresh = new Simulation(w, h);
            bodies.addAll(fresh.bodies);
        }
    }

    private void addBody(float x, float y, float vx, float vy, float mass, boolean comet) {
        Body b = new Body();
        b.x = x;
        b.y = y;
        b.vx = vx;
        b.vy = vy;
        b.mass = mass;
        b.radius = radiusForMass(mass);
        b.stage = stageForParticles(b.particleCount);
        b.color = PlanetStyling.colorForStage(b.stage);
        b.isComet = comet;
        b.particleCount = Math.max(1, Math.round(mass));
        b.removed = false;
        bodies.add(b);
    }

    public SimulationSnapshot snapshot() {
        synchronized (bodies) {
            List<SimulationSnapshot.BodyView> copy = new ArrayList<>(bodies.size());
            for (Body b : bodies) {
                SimulationSnapshot.BodyView v = new SimulationSnapshot.BodyView(
                        b.x, b.y, b.vx, b.vy, b.radius, b.color, b.isSun, b.name, b.particleCount, b.isComet
                );
                copy.add(v);
            }
            return new SimulationSnapshot(copy, autoCometsEnabled);
        }
    }

    private Body[] topGiants() {
        Body[] top = new Body[MAX_GIANTS];
        float[] masses = new float[MAX_GIANTS];
        for (int i = 1; i < bodies.size(); i++) { // skip sun
            Body b = bodies.get(i);
            int idx = 0;
            for (int k = 1; k < MAX_GIANTS; k++) {
                if (top[k] == null || masses[k] < masses[idx]) {
                    idx = k;
                }
            }
            if (top[idx] == null || b.mass > masses[idx]) {
                top[idx] = b;
                masses[idx] = b.mass;
            }
        }
        return top;
    }

    public void sprinkleDust(float x, float y, int count) {
        synchronized (bodies) {
            Body sun = bodies.get(0);
            for (int i = 0; i < count; i++) {
                float mass = 0.25f + random.nextFloat() * 0.9f;
                float dx = x - sun.x;
                float dy = y - sun.y;
                float r = (float) Math.sqrt(dx * dx + dy * dy) + 1f;
                float angle = (float) Math.atan2(dy, dx);
                float speed = (float) Math.sqrt((G * sun.mass) / r) * (0.90f + random.nextFloat() * 0.18f);
                float vx = (float) (-Math.sin(angle) * speed) + (random.nextFloat() - 0.5f) * 0.16f;
                float vy = (float) (Math.cos(angle) * speed) + (random.nextFloat() - 0.5f) * 0.16f;
                addBody(x + (random.nextFloat() - 0.5f) * 6f, y + (random.nextFloat() - 0.5f) * 6f, vx, vy, mass, false);
            }
        }
    }

    public void applyWind(float x, float y, float scale) {
        synchronized (bodies) {
            float radius = 200f;
            float baseForce = 0.05f * scale;
            for (int i = 1; i < bodies.size(); i++) {
                Body b = bodies.get(i);
                float dx = b.x - x;
                float dy = b.y - y;
                float distSq = dx * dx + dy * dy;
                if (distSq > radius * radius) continue;
                float dist = (float) Math.sqrt(distSq) + 1f;
                float falloff = 1f - (dist / radius);
                float force = baseForce * falloff;
                b.vx += (dx / dist) * force;
                b.vy += (dy / dist) * force;
            }
        }
    }

    public void applyGravityGlove(float x, float y, float scale, int extraDust) {
        synchronized (bodies) {
            Body sun = bodies.get(0);
            float radius = 220f;
            float baseForce = 0.06f * scale;
            for (int i = 1; i < bodies.size(); i++) {
                Body b = bodies.get(i);
                float dx = x - b.x;
                float dy = y - b.y;
                float distSq = dx * dx + dy * dy;
                if (distSq > radius * radius) continue;
                float dist = (float) Math.sqrt(distSq) + 1f;
                float falloff = 1f - (dist / radius);
                float force = baseForce * falloff;
                b.vx += (dx / dist) * force;
                b.vy += (dy / dist) * force;
            }
            for (int i = 0; i < extraDust; i++) {
                float mass = 0.25f + random.nextFloat() * 0.9f;
                float dx = x - sun.x;
                float dy = y - sun.y;
                float r = (float) Math.sqrt(dx * dx + dy * dy) + 1f;
                float angle = (float) Math.atan2(dy, dx);
                float speed = (float) Math.sqrt((G * sun.mass) / r) * (0.90f + random.nextFloat() * 0.18f);
                float vx = (float) (-Math.sin(angle) * speed) + (random.nextFloat() - 0.5f) * 0.14f;
                float vy = (float) (Math.cos(angle) * speed) + (random.nextFloat() - 0.5f) * 0.14f;
                addBody(x + (random.nextFloat() - 0.5f) * 4f, y + (random.nextFloat() - 0.5f) * 4f, vx, vy, mass, false);
            }
        }
    }

    public void launchComet() {
        synchronized (bodies) {
            Body sun = bodies.get(0);
            float x, y, vx, vy;
            float speed = 10f + random.nextFloat() * 5f;
            int side = random.nextInt(4);
            if (side == 0) { x = -80f; y = random.nextFloat() * height; }
            else if (side == 1) { x = width + 80f; y = random.nextFloat() * height; }
            else if (side == 2) { x = random.nextFloat() * width; y = -80f; }
            else { x = random.nextFloat() * width; y = height + 80f; }
            float dx = sun.x - x;
            float dy = sun.y - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy) + 1f;
            vx = (dx / dist) * speed + (random.nextFloat() - 0.5f) * 0.6f;
            vy = (dy / dist) * speed + (random.nextFloat() - 0.5f) * 0.6f;
            float mass = 120f + random.nextFloat() * 80f;
            addBody(x, y, vx, vy, mass, true);
            listeners.forEach(l -> l.onSound(SoundEvent.COMET));
            listeners.forEach(l -> l.onCaption("Comet incoming!", 2200));
        }
    }

    public void shakeDisk() {
        synchronized (bodies) {
            for (int i = 1; i < bodies.size(); i++) {
                Body b = bodies.get(i);
                b.vx += (random.nextFloat() - 0.5f) * 0.8f;
                b.vy += (random.nextFloat() - 0.5f) * 0.8f;
            }
        }
    }

    public void cleanUpDust() {
        synchronized (bodies) {
            float cx = width / 2f;
            float cy = height / 2f;
            float maxR = Math.min(width, height) * 0.6f;
            for (int i = 1; i < bodies.size(); i++) {
                Body b = bodies.get(i);
                float dx = b.x - cx;
                float dy = b.y - cy;
                float distSq = dx * dx + dy * dy;
                if (distSq > maxR * maxR && b.mass < 20f) {
                    b.removed = true;
                }
            }
            compact();
        }
    }

    public void setAutoCometsEnabled(boolean enabled) {
        autoCometsEnabled = enabled;
        if (enabled) {
            scheduleAutoComet();
        }
    }

    public boolean isAutoCometsEnabled() {
        return autoCometsEnabled;
    }

    public void rebuildForSize(int w, int h) {
        synchronized (bodies) {
            this.width = w;
            this.height = h;
            bodies.clear();
            initBodies();
        }
    }

    public void recenterTo(int targetW, int targetH) {
        synchronized (bodies) {
            this.width = targetW;
            this.height = targetH;
            Body sun = bodies.get(0);
            float targetX = targetW / 2f;
            float targetY = targetH / 2f;
            float dx = targetX - sun.x;
            float dy = targetY - sun.y;
            if (Math.abs(dx) < 0.01f && Math.abs(dy) < 0.01f) return;
            for (Body b : bodies) {
                b.x += dx;
                b.y += dy;
            }
        }
    }

    private void step() {
        synchronized (bodies) {
            Body sun = bodies.get(0);
            Body[] giants = topGiants();
            int count = bodies.size();
            IntStream.range(1, count).parallel().forEach(i -> {
                Body b = bodies.get(i);
                b.removed = false;
                float dx = sun.x - b.x;
                float dy = sun.y - b.y;
                float distSq = dx * dx + dy * dy + GRAVITY_SOFTENING;
                float dist = (float) Math.sqrt(distSq);
                float accel = (G * sun.mass / distSq);
                b.vx += accel * dx / dist;
                b.vy += accel * dy / dist;
                for (Body giant : giants) {
                    if (giant == null || giant == b) continue;
                    float gdx = giant.x - b.x;
                    float gdy = giant.y - b.y;
                    float gDistSq = gdx * gdx + gdy * gdy + GRAVITY_SOFTENING;
                    float gDist = (float) Math.sqrt(gDistSq);
                    float gAccel = (G * PLANET_GRAVITY_SCALE * giant.mass / gDistSq);
                    b.vx += gAccel * gdx / gDist;
                    b.vy += gAccel * gdy / gDist;
                }
                b.vx *= DRAG;
                b.vy *= DRAG;
                b.x += b.vx;
                b.y += b.vy;
            });
            clearGrid();
            for (int i = 1; i < bodies.size(); i++) bucketBody(bodies.get(i));
            resolveCollisions();
            compact();
            recenter();
            physicsTick++;
            if (physicsTick % 45 == 0) checkDenseDust();
        }
    }

    private void clearGrid() {
        for (List<Body> cell : grid) cell.clear();
    }

    private void bucketBody(Body b) {
        int col = clamp((int) (b.x / CELL_SIZE), 0, gridCols - 1);
        int row = clamp((int) (b.y / CELL_SIZE), 0, gridRows - 1);
        grid.get(row * gridCols + col).add(b);
    }

    private void resolveCollisions() {
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                List<Body> cell = grid.get(row * gridCols + col);
                for (int i = 0; i < cell.size(); i++) {
                    Body a = cell.get(i);
                    if (a.removed) continue;
                    for (int j = i + 1; j < cell.size(); j++) {
                        Body b = cell.get(j);
                        if (b.removed) continue;
                        if (close(a, b)) mergeIntoFirst(a, b);
                    }
                }
                checkNeighbor(cell, col + 1, row);
                checkNeighbor(cell, col + 1, row + 1);
                checkNeighbor(cell, col, row + 1);
                checkNeighbor(cell, col - 1, row + 1);
            }
        }
    }

    private void checkNeighbor(List<Body> cell, int col, int row) {
        if (col < 0 || col >= gridCols || row < 0 || row >= gridRows) return;
        List<Body> neighbor = grid.get(row * gridCols + col);
        if (neighbor.isEmpty() || cell.isEmpty()) return;
        for (Body a : cell) {
            if (a.removed) continue;
            for (Body b : neighbor) {
                if (b.removed || a == b) continue;
                if (close(a, b)) mergeIntoFirst(a, b);
            }
        }
    }

    private boolean close(Body a, Body b) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float min = (a.radius + b.radius) * 1.35f;
        return dx * dx + dy * dy <= min * min;
    }

    private void mergeIntoFirst(Body a, Body b) {
        float newMass = a.mass + b.mass;
        float newX = (a.x * a.mass + b.x * b.mass) / newMass;
        float newY = (a.y * a.mass + b.y * b.mass) / newMass;
        float newVx = (a.vx * a.mass + b.vx * b.mass) / newMass;
        float newVy = (a.vy * a.mass + b.vy * b.mass) / newMass;
        a.mass = newMass;
        a.x = newX;
        a.y = newY;
        a.vx = newVx + (random.nextFloat() - 0.5f) * 0.03f;
        a.vy = newVy + (random.nextFloat() - 0.5f) * 0.03f;
        a.radius = radiusForMass(newMass);
        a.particleCount += b.particleCount;
        // Preserve existing name if present; otherwise inherit from b if available
        if (a.name == null && b.name != null) {
            a.name = b.name;
        }
        // Comet tails stop once merged with any non-comet body
        a.isComet = a.isComet && b.isComet;
        updateStageAndName(a);
        if (shouldPlayMergeChime(a, b) && canPlayMergeChime()) {
            listeners.forEach(l -> l.onSound(SoundEvent.MERGE));
        }
        b.removed = true;
    }

    private void compact() {
        List<Body> keep = new ArrayList<>(bodies.size());
        keep.add(bodies.get(0));
        for (int i = 1; i < bodies.size(); i++) {
            Body b = bodies.get(i);
            if (!b.removed) keep.add(b);
        }
        bodies.clear();
        bodies.addAll(keep);
    }

    private void recenter() {
        Body sun = bodies.get(0);
        float targetX = width / 2f;
        float targetY = height / 2f;
        float dx = targetX - sun.x;
        float dy = targetY - sun.y;
        if (Math.abs(dx) < 0.01f && Math.abs(dy) < 0.01f) return;
        for (Body b : bodies) {
            b.x += dx;
            b.y += dy;
        }
    }

    private void checkDenseDust() {
        Body sun = bodies.get(0);
        int dense = 0;
        float nearR = Math.min(width, height) * 0.18f;
        float nearRSq = nearR * nearR;
        for (int i = 1; i < bodies.size(); i++) {
            Body b = bodies.get(i);
            float dx = b.x - sun.x;
            float dy = b.y - sun.y;
            if (dx * dx + dy * dy < nearRSq) dense++;
        }
        if (dense > 5000) {
            listeners.forEach(l -> l.onCaption("So much dust near the sun!", 3500));
        } else if (dense > 2000) {
            listeners.forEach(l -> l.onCaption("Dust cloud crowding the sun.", 3000));
        }
    }

    private float radiusForMass(float mass) {
        return (float) Math.max(0.6, 0.4 + Math.cbrt(mass) * 0.55);
    }

    private Stage stageForParticles(long particles) {
        if (particles >= 30_000) return Stage.MYSTERY;
        if (particles >= 15_000) return Stage.GARDEN;
        if (particles >= 7_000) return Stage.OCEAN;
        return Stage.ROCK;
    }

    private void updateStageAndName(Body b) {
        Stage newStage = stageForParticles(b.particleCount);
        if (b.stage != newStage) {
            b.stage = newStage;
            b.color = PlanetStyling.colorForStage(newStage);
            if (b.name != null) {
                switch (newStage) {
                    case ROCK -> listeners.forEach(l -> l.onCaption(b.name + " is rocky!", 2500));
                    case OCEAN -> listeners.forEach(l -> l.onCaption(b.name + " is sloshy with oceans!", 2500));
                    case GARDEN -> listeners.forEach(l -> l.onCaption(b.name + " is turning green!", 2500));
                    case MYSTERY -> listeners.forEach(l -> l.onCaption(b.name + " is a mystery world now!", 2500));
                    default -> { }
                }
            }
        }
        if (b.name == null && b.particleCount >= 5_000) {
            b.name = PlanetStyling.nextName();
            listeners.forEach(l -> l.onCaption("New planet: " + b.name + "!", 3000));
        }
    }

    private boolean shouldPlayMergeChime(Body a, Body b) {
        return (a.name != null || b.name != null || a.particleCount >= 5_000 || b.particleCount >= 5_000);
    }

    private boolean canPlayMergeChime() {
        long now = System.currentTimeMillis();
        if (now - lastMergeChimeMs < 150) return false;
        lastMergeChimeMs = now;
        return true;
    }

    private int clamp(int val, int min, int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    private void scheduleAutoComet() {
        if (scheduler == null) return;
        scheduler.schedule(() -> {
            if (autoCometsEnabled) {
                launchComet();
            }
            if (running) scheduleAutoComet();
        }, nextCometDelayMs(), TimeUnit.MILLISECONDS);
    }

    private int nextCometDelayMs() {
        return 12_000 + random.nextInt(12_000);
    }
}
